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
import com.github.exact7.xtra.util.DownloadUtils
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
import com.tonyodev.fetch2.Request
import dagger.android.AndroidInjection
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

const val TAG = "DownloadService"
const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
const val KEY_REQUEST = "request"
const val KEY_TYPE = "type"

class DownloadService : IntentService(TAG), Injectable {

    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var offlineRepository: OfflineRepository
    var stopped = false

    init {
        setIntentRedelivery(true)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Starting download")
        val request = with(intent!!) {
            Gson().fromJson(getStringExtra(KEY_REQUEST), if (getBooleanExtra(KEY_TYPE, true)) VideoRequest::class.java else ClipRequest::class.java)
        }
        val offlineVideo = runBlocking { offlineRepository.getVideoById(request.offlineVideoId) } ?: return //Download was canceled
        val countDownLatch = CountDownLatch(1)
        val fetch = DownloadUtils.getFetch(this)
        val channelId = getString(R.string.notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(android.R.drawable.stat_sys_download)
            setGroup(GROUP_KEY)
            setContentTitle(getString(R.string.downloading))
            setOngoing(true)
            setContentText(offlineVideo.name)
            val clickIntent = Intent(this@DownloadService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("code", 0)
            }
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            val cancelIntent = Intent(this@DownloadService, CancelActionReceiver::class.java)
            addAction(NotificationCompat.Action(0, getString(android.R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
        }

        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                NotificationChannel(channelId, getString(R.string.notification_downloads_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                    manager.createNotificationChannel(this)
                }
            }
        }
        var canceled = false
        when (request) {
            is VideoRequest -> with(request) {
                notificationBuilder.apply {
                    setProgress(maxProgress, 0, false)
                }
                notificationManager.notify(id, notificationBuilder.build())
                fetch.addListener(object : AbstractFetchListener() {
                    var activeDownloadsCount = 0

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                        activeDownloadsCount++
                    }

                    override fun onCompleted(download: Download) {
                        if (++progress < maxProgress) {
                            notificationManager.notify(id, notificationBuilder.setProgress(maxProgress, progress, false).build())
                        } else {
                            onDownloadCompleted(request, offlineVideo, notificationManager, notificationBuilder)
                            countDownLatch.countDown()
                        }
                    }

                    override fun onDeleted(download: Download) {
                        if (--activeDownloadsCount == 0) {
                            offlineRepository.deleteVideo(offlineVideo)
                            val directory = File(path)
                            if (directory.exists() && directory.list().isEmpty()) {
                                directory.deleteRecursively()
                            }
                            canceled = true
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
                notificationBuilder.setProgress(100, 0, false)
                fetch.addListener(object : AbstractFetchListener() {
                    override fun onCompleted(download: Download) {
                        onDownloadCompleted(request, offlineVideo, notificationManager, notificationBuilder)
                        countDownLatch.countDown()
                    }

                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                        notificationManager.notify(id, notificationBuilder.setProgress(100, download.progress, false).build())
                    }

                    override fun onDeleted(download: Download) {
                        offlineRepository.deleteVideo(offlineVideo)
                        canceled = true
                        countDownLatch.countDown()
                    }
                })
                fetch.enqueue(Request(url, path).apply { groupId = offlineVideoId })
            }
        }
        startForeground(request.id, notificationBuilder.build())
        countDownLatch.await()
        fetch.close()
        stopForegroundInternal(canceled)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!stopped) {
            stopForegroundInternal(true)
        }
    }

    private fun stopForegroundInternal(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(if (removeNotification) Service.STOP_FOREGROUND_REMOVE else Service.STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(removeNotification)
        }
        stopped = true
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(
            request: com.github.exact7.xtra.model.offline.Request,
            offlineVideo: OfflineVideo,
            notificationManager: NotificationManagerCompat,
            notificationBuilder: NotificationCompat.Builder) {
        when (request) {
            is VideoRequest -> {
                Log.d(TAG, "Downloaded video")
                with(request) {
                    val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
                        fun parse(trackData: TrackData) =
                                trackData.uri.substring(trackData.uri.lastIndexOf('/') + 1, trackData.uri.lastIndexOf('.')).let { trackName ->
                                    if (!trackName.endsWith("muted")) trackName.toInt() else trackName.substringBefore('-').toInt()
                                }

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
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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
    }
}