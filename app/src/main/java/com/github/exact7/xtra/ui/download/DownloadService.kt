package com.github.exact7.xtra.ui.download

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.FetchProvider
import com.google.gson.Gson
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.ParsingMode
import com.iheartradio.m3u8.PlaylistParser
import com.iheartradio.m3u8.PlaylistWriter
import com.iheartradio.m3u8.data.MediaPlaylist
import com.iheartradio.m3u8.data.Playlist
import com.iheartradio.m3u8.data.TrackData
import com.iheartradio.m3u8.data.TrackInfo
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import dagger.android.AndroidInjection
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Comparator
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

const val TAG = "DownloadService"
const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
const val KEY_REQUEST = "request"
const val KEY_TYPE = "type"

class DownloadService : IntentService(TAG), Injectable {

    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var offlineRepository: OfflineRepository
    @Inject lateinit var fetchProvider: FetchProvider
    private lateinit var fetch: Fetch
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var request: com.github.exact7.xtra.model.offline.Request
    private lateinit var offlineVideo: OfflineVideo
    private var stopped = false
    private var completed = false

    init {
        setIntentRedelivery(true)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Starting download")
        request = with(intent!!) {
            Gson().fromJson(getStringExtra(KEY_REQUEST), if (getBooleanExtra(KEY_TYPE, true)) VideoRequest::class.java else ClipRequest::class.java)
        }
        offlineVideo = runBlocking { offlineRepository.getVideoById(request.offlineVideoId) } ?: return //Download was canceled
        val countDownLatch = CountDownLatch(1)
        fetch = fetchProvider.get()
        val channelId = getString(R.string.notification_channel_id)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(android.R.drawable.stat_sys_download)
            setGroup(GROUP_KEY)
            setContentTitle(getString(R.string.downloading))
            setOngoing(true)
            setContentText(offlineVideo.name)
            val clickIntent = Intent(this@DownloadService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("code", 0)
            }
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            val cancelIntent = Intent(this@DownloadService, NotificationActionReceiver::class.java)
            addAction(NotificationCompat.Action(0, getString(android.R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
        }

        notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                NotificationChannel(channelId, getString(R.string.notification_downloads_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                    manager.createNotificationChannel(this)
                }
            }
        }
        when (request) {
            is VideoRequest -> with(request as VideoRequest) {
                updateProgress(maxProgress, 0)
                fetch.addListener(object : AbstractFetchListener() {
                    var activeDownloadsCount = 0

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                        activeDownloadsCount++
                    }

                    override fun onCompleted(download: Download) {
                        if (++progress < maxProgress) {
                            updateProgress(maxProgress, progress)
                        } else {
                            onDownloadCompleted()
                            countDownLatch.countDown()
                        }
                    }

                    override fun onCancelled(download: Download) {
                        stopForegroundInternal(true)
                    }

                    override fun onDeleted(download: Download) {
                        if (--activeDownloadsCount == 0) {
                            offlineRepository.deleteVideo(offlineVideo)
                            val directory = File(path)
                            if (directory.exists() && directory.list().isEmpty()) {
                                directory.deleteRecursively()
                            }
                            countDownLatch.countDown()
                        }
                    }
                })
                playerRepository.fetchVideoPlaylist(videoId)
                        .map { response ->
                            val playlist = response.body()!!.string()
                            URL("https://.*\\.m3u8".toRegex().find(playlist)!!.value).openStream().use {
                                PlaylistParser(it, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                            }
                        }
                        .subscribe({ p ->
                            playlist = p
                            val requests = p.tracks.subList(segmentFrom, segmentTo).map {
                                Request(url + it.uri, path + it.uri).apply { groupId = offlineVideoId }
                            }
                            fetch.enqueue(requests)
                        }, {

                        })
            }
            is ClipRequest -> with(request) {
                updateProgress(100, 0)
                fetch.addListener(object : AbstractFetchListener() {
                    override fun onCompleted(download: Download) {
                        onDownloadCompleted()
                        countDownLatch.countDown()
                    }

                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                        updateProgress(100, download.progress)
                    }

                    override fun onCancelled(download: Download) {
                        stopForegroundInternal(true)
                    }

                    override fun onDeleted(download: Download) {
                        offlineRepository.deleteVideo(offlineVideo)
                        countDownLatch.countDown()
                    }
                })
                fetch.enqueue(Request(url, path).apply { groupId = offlineVideoId })
            }
        }
        startForeground(request.id, notificationBuilder.build())
        countDownLatch.await()
        fetch.close()
    }

    override fun onDestroy() {
        stopForegroundInternal(true)
        if (!completed) {
            if (this::fetch.isInitialized && !fetch.isClosed && fetch.hasActiveDownloads) {
                fetch.deleteAll()
            }
            offlineRepository.deleteVideo(offlineVideo)
        }
        super.onDestroy()
    }

    private fun stopForegroundInternal(removeNotification: Boolean) {
        if (!stopped) {
            stopped = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(if (removeNotification) Service.STOP_FOREGROUND_REMOVE else Service.STOP_FOREGROUND_DETACH)
            } else {
                stopForeground(removeNotification)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted() {
        when (request) {
            is VideoRequest -> {
                Log.d(TAG, "Downloaded video")
                with(request as VideoRequest) {
                    val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
                        fun parse(trackData: TrackData) =
                                trackData.uri.substring(trackData.uri.lastIndexOf('/') + 1, trackData.uri.lastIndexOf('.')).substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf('.')).filter { it.isDigit() }.toInt()

                        val index1 = parse(o1)
                        val index2 = parse(o2)
                        when {
                            index1 > index2 -> 1
                            index1 < index2 -> -1
                            else -> 0
                        }
                    })
                    for (i in segmentFrom until segmentTo) {
                        val track = playlist.tracks[i]
                        tracks.add(TrackData.Builder()
                                .withUri("$path${track.uri}")
                                .withTrackInfo(TrackInfo(track.trackInfo.duration, track.trackInfo.title))
                                .build())
                    }
                    val mediaPlaylist = MediaPlaylist.Builder()
                            .withTargetDuration(playlist.targetDuration)
                            .withTracks(tracks.toList())
                            .build()
                    val playlist = Playlist.Builder()
                            .withMediaPlaylist(mediaPlaylist)
                            .build()
                    FileOutputStream(offlineVideo.url).use {
                        PlaylistWriter(it, Format.EXT_M3U, Encoding.UTF_8).write(playlist)
                    }
                    Log.d(TAG, "Playlist created")
                }
            }
            is ClipRequest -> Log.d(TAG, "Downloaded clip")
        }
        offlineRepository.onDownloaded(offlineVideo)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("video", offlineVideo)
            putExtra("code", 1)
        }
        notificationBuilder.apply {
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            mActions.clear()
        }
        notificationManager.notify(request.id, notificationBuilder.build())
        completed = true
        stopForegroundInternal(false)
    }

    private fun updateProgress(maxProgress: Int, progress: Int) {
        notificationManager.notify(request.id, notificationBuilder.setProgress(maxProgress, progress, false).build())
    }
}