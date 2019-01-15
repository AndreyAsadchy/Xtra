package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import java.util.Comparator
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

const val TAG = "DownloadWorker"
const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
const val KEY_REQUEST = "request"
const val KEY_TYPE = "type"

class DownloadService : IntentService(TAG), Injectable {

    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var offlineRepository: OfflineRepository

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
        val countDownLatch = CountDownLatch(1)
        val fetch = DownloadUtils.getFetch(this)
        val channelId = getString(R.string.notification_channel_id)
        val offlineVideo = runBlocking { offlineRepository.getVideoById(request.offlineVideoId) }
        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            setGroup(GROUP_KEY)
            setContentTitle(getString(R.string.downloading))
            setOngoing(true)
            setContentText(offlineVideo.name)
            val clickIntent = Intent(this@DownloadService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 0, clickIntent, 0))
            val cancelIntent = Intent(this@DownloadService, CancelActionReceiver::class.java).putExtra("id", request.id)
            addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
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

        when (request) {
            is VideoRequest -> with(request) {
                notificationBuilder.apply {
                    setProgress(maxProgress, 0, false)
                }
                notificationManager.notify(id, notificationBuilder.build())
                fetch.addListener(object : AbstractFetchListener() {
                    var queuedCount = 0

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                        queuedCount++
                    }

                    override fun onCompleted(download: Download) {
                        offlineVideo.downloadProgress.set(++progress)
                        if (progress < maxProgress) {
                            notificationManager.notify(id, notificationBuilder.setProgress(maxProgress, progress, false).build())
                        } else {
                            onDownloadCompleted(request, offlineVideo, notificationManager, notificationBuilder)
                            countDownLatch.countDown()
                        }
                    }

                    override fun onDeleted(download: Download) {
                        if (--queuedCount == 0) {
                            val directory = File(path)
                            if (directory.exists() && directory.list().isEmpty()) {
                                directory.deleteRecursively()
                                offlineRepository.deleteVideo(offlineVideo)
                                countDownLatch.countDown()
                            }
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
                            val requests = p.tracks.subList(segmentFrom, segmentTo).map { Request(url + it.uri, path + it.uri) }
                            fetch.enqueue(requests)
                        }, {

                        })
            }
            is ClipRequest -> with(request) {
                fetch.addListener(object : AbstractFetchListener() {
                    override fun onCompleted(download: Download) {
                        onDownloadCompleted(request, offlineVideo, notificationManager, notificationBuilder)
                        countDownLatch.countDown()
                    }

                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                        val downloadProgress = download.progress
                        notificationManager.notify(id, notificationBuilder.setProgress(100, downloadProgress, false).build())
                        offlineVideo.downloadProgress.set(downloadProgress)
                    }

                    override fun onCancelled(download: Download) {
                        offlineRepository.deleteVideo(offlineVideo)
                        countDownLatch.countDown()
                    }
                })
                fetch.enqueue(Request(url, path))
            }
        }
        startForeground(request.id, notificationBuilder.build())
        countDownLatch.await()
        fetch.close()
        stopForeground(false)
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
                    for (i in segmentFrom..segmentTo) {
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
                    val playlistPath = "$path$id.m3u8"
                    FileOutputStream(playlistPath).use {
                        PlaylistWriter(it, Format.EXT_M3U, Encoding.UTF_8).write(playlist)
                    }
                    Log.d(TAG, "Playlist created")
                }
            }
            is ClipRequest -> Log.d(TAG, "Downloaded clip")
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("video", offlineVideo)
        }
        notificationBuilder.apply {
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            setContentIntent(PendingIntent.getActivity(this@DownloadService, 1, intent, 0))
            mActions.clear()
        }
        notificationManager.notify(request.id, notificationBuilder.build())
    }
}