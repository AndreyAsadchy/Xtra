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
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.FetchProvider
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
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import kotlin.math.min


class DownloadService : IntentService(TAG), Injectable {

    companion object {
        private const val TAG = "DownloadService"
        private const val ENQUEUE_SIZE = 15

        const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
        const val KEY_REQUEST = "request"
        const val KEY_WIFI = "wifi"

        val activeRequests = HashSet<Int>()
    }

    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var offlineRepository: OfflineRepository
    @Inject lateinit var fetchProvider: FetchProvider
    private lateinit var fetch: Fetch
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var request: com.github.exact7.xtra.model.offline.Request
    private lateinit var offlineVideo: OfflineVideo

    private lateinit var playlist: MediaPlaylist

    init {
        setIntentRedelivery(true)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    @SuppressLint("CheckResult")
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Starting download")
        request = intent!!.getParcelableExtra(KEY_REQUEST)
        offlineVideo = runBlocking { offlineRepository.getVideoByIdAsync(request.offlineVideoId).await() } ?: return //Download was canceled
        fetch = fetchProvider.get(offlineVideo.id, intent.getBooleanExtra(KEY_WIFI, false))
        val countDownLatch = CountDownLatch(1)
        val channelId = getString(R.string.notification_channel_id)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            priority = NotificationCompat.PRIORITY_LOW
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
            val cancelIntent = Intent(this@DownloadService, NotificationActionReceiver::class.java).putExtra(NotificationActionReceiver.KEY_VIDEO_ID, offlineVideo.id)
            addAction(NotificationCompat.Action(0, getString(android.R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
        }

        notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                NotificationChannel(channelId, getString(R.string.notification_downloads_channel), NotificationManager.IMPORTANCE_LOW).apply {
                    manager.createNotificationChannel(this)
                }
            }
        }
        println("INIT ${offlineVideo.progress}")
        updateProgress(offlineVideo.maxProgress, offlineVideo.progress)
        if (offlineVideo.vod) {
            fetch.addListener(object : AbstractFetchListener() {
                var activeDownloadsCount = 0

                override fun onAdded(download: Download) {
                    activeDownloadsCount++
                }

                override fun onCompleted(download: Download) {
                    with (offlineVideo) {
                        if (++progress < maxProgress) {
                            Log.d(TAG, "$progress / $maxProgress")
                            updateProgress(maxProgress, progress)
                            if (--activeDownloadsCount == 0) {
                                enqueueNext()
                            }
                        } else {
                            onDownloadCompleted()
                            countDownLatch.countDown()
                        }
                    }
                }

                override fun onDeleted(download: Download) {
                    if (--activeDownloadsCount == 0) {
                        offlineRepository.deleteVideo(offlineVideo)
                        stopForegroundInternal(true)
                        val directory = File(request.path)
                        if (directory.exists() && directory.list().isEmpty()) {
                            directory.deleteRecursively()
                        }
                        countDownLatch.countDown()
                    }
                }
            })
            playerRepository.loadVideoPlaylist(request.videoId!!)
                    .map { response ->
                        val playlist = response.body()!!.string()
                        URL("https://.*\\.m3u8".toRegex().find(playlist)!!.value).openStream().use {
                            PlaylistParser(it, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                        }
                    }
                    .subscribeBy(onSuccess = {
                        playlist = it
                        enqueueNext()
                    })
        } else {
            fetch.addListener(object : AbstractFetchListener() {
                override fun onCompleted(download: Download) {
                    onDownloadCompleted()
                    countDownLatch.countDown()
                }

                override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                    updateProgress(100, download.progress)
                }

                override fun onDeleted(download: Download) {
                    offlineRepository.deleteVideo(offlineVideo)
                    stopForegroundInternal(true)
                    countDownLatch.countDown()
                }
            })
            fetch.enqueue(Request(request.url, request.path).apply { groupId = request.offlineVideoId })
        }
        offlineRepository.updateVideo(offlineVideo.apply { status = OfflineVideo.STATUS_DOWNLOADING })
        startForeground(offlineVideo.id, notificationBuilder.build())
        countDownLatch.await()
        activeRequests.remove(request.offlineVideoId)
        fetch.close()
    }

    private fun enqueueNext() {
        val requests = mutableListOf<Request>()
        val tracks: List<TrackData>
        try {
            tracks = playlist.tracks
        } catch (e: UninitializedPropertyAccessException) {
            GlobalScope.launch {
                delay(3000L)
                enqueueNext()
            }
            return
        }
        with(request) {
            val current = segmentFrom!! + offlineVideo.progress
            try {
                for (i in current..min(current + ENQUEUE_SIZE, segmentTo!!)) {
                    val track = tracks[i]
                    requests.add(Request(url + track.uri, path + track.uri).apply { groupId = offlineVideoId })
                }
            } catch (e: IndexOutOfBoundsException) {
                Crashlytics.logException(e)
                Crashlytics.log("DownloadService.enqueueNext: Playlist tracks size: ${playlist.tracks.size}. Segment to: $segmentTo. Current + ENQUEUE_SIZE: ${current + ENQUEUE_SIZE}.")
                offlineRepository.updateVideo(offlineVideo.apply { maxProgress = tracks.size })
            }
        }
        fetch.enqueue(requests)
    }

    private fun stopForegroundInternal(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(if (removeNotification) Service.STOP_FOREGROUND_REMOVE else Service.STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(removeNotification)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted() {
        if (offlineVideo.vod) {
            Log.d(TAG, "Downloaded video")
            with(request) {
                val tracks = ArrayList<TrackData>(offlineVideo.maxProgress)
                try {
                    for (i in segmentFrom!!..segmentTo!!) {
                        val track = playlist.tracks[i] //TODO encrypt files
                        tracks.add(TrackData.Builder()
                                .withUri("$path${track.uri}")
                                .withTrackInfo(TrackInfo(track.trackInfo.duration, track.trackInfo.title))
                                .build())
                    }
                } catch (e: UninitializedPropertyAccessException) {
                    GlobalScope.launch {
                        delay(3000L)
                        onDownloadCompleted()
                    }
                    return
                }
                val mediaPlaylist = MediaPlaylist.Builder()
                        .withTargetDuration(playlist.targetDuration)
                        .withTracks(tracks)
                        .build()
                val playlist = Playlist.Builder()
                        .withMediaPlaylist(mediaPlaylist)
                        .build()
                FileOutputStream(offlineVideo.url).use {
                    PlaylistWriter(it, Format.EXT_M3U, Encoding.UTF_8).write(playlist)
                }
                Log.d(TAG, "Playlist created")
            }
        } else {
            Log.d(TAG, "Downloaded clip")
        }
        offlineRepository.updateVideo(offlineVideo.apply { status = OfflineVideo.STATUS_DOWNLOADED})
        offlineRepository.deleteRequest(request)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("video", offlineVideo)
            putExtra("code", 1)
        }
        notificationBuilder.apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            mActions.clear()
        }
        notificationManager.notify(offlineVideo.id, notificationBuilder.build())
        stopForegroundInternal(false)
    }

    private fun updateProgress(maxProgress: Int, progress: Int) {
        notificationManager.notify(offlineVideo.id, notificationBuilder.setProgress(maxProgress, progress, false).build())
        println("${offlineVideo.progress} $progress")
        if (offlineVideo.progress != progress) {
            offlineVideo.progress = progress
            println("PROGRESS $progress")
            offlineRepository.updateVideo(offlineVideo)
            runBlocking { offlineRepository.getVideoByIdAsync(offlineVideo.id).await() }
        }
    }
}