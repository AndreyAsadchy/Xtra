package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.main.MainActivity
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

class DownloadService : IntentService(TAG) {

    companion object {
        fun download(context: Context, request: Request) {
            val intent = Intent(context, DownloadService::class.java)
                    .putExtra(KEY_REQUEST, Gson().toJson(request))
                    .putExtra(KEY_TYPE, request is VideoRequest)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    @Inject lateinit var repository: PlayerRepository
    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadReceiver: BroadcastReceiver
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var request: Request
    private lateinit var countDownLatch: CountDownLatch

    init {
        setIntentRedelivery(true)
    }

    fun init() {
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val channelId = getString(R.string.notification_channel_id)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            setGroup(GROUP_KEY)
            setContentTitle(getString(R.string.downloading))
            setOngoing(true)
            val cancelIntent = Intent(this@DownloadService, CancelActionReceiver::class.java)
            addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, 0, cancelIntent, 0)))
        }

        notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channelName = manager.getNotificationChannel(channelId)
            if (channelName == null) {
                channelName = NotificationChannel(channelId, getString(R.string.notification_downloads_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                    manager.createNotificationChannel(channelName)
                }

            }
        }

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (request) {
                    is VideoRequest -> with(request) {
                        if (!canceled) {
                            if (++progress < maxProgress) {
                                enqueueNextSegments(1)
                                notificationBuilder.setProgress(maxProgress, progress, false)
                                notificationManager.notify(offlineVideo.id, notificationBuilder.build())
                            } else {
                                onDownloadCompleted(this)
                            }
                        } else {
                            val directory = File(path.path)
                            if (directory.exists() && directory.list().isEmpty()) {
                                directory.deleteRecursively()
                                countDownLatch.countDown()
                            }
                        }
                    }
                    is ClipRequest -> {
                        if (!request.canceled) {
                            onDownloadCompleted(request)
                        } else {
                            countDownLatch.countDown()
                        }
                    }
                }
            }
        }

        countDownLatch = CountDownLatch(1)

        val downloadHandler = HandlerThread("RequestThread", Process.THREAD_PRIORITY_BACKGROUND).run {
            start()
            Handler(looper)
        }
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), null, downloadHandler)
    }


    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Starting download")
        request = with(intent!!) {
            Gson().fromJson(getStringExtra(KEY_REQUEST), if (getBooleanExtra(KEY_TYPE, true)) VideoRequest::class.java else ClipRequest::class.java)
        }
        init()
        when (request) {
            is VideoRequest -> with(request as VideoRequest) {
                notificationBuilder.apply {
                    setContentText(offlineVideo.name)
                    setProgress(maxProgress, progress, false)
                }
                notificationManager.notify(offlineVideo.id, notificationBuilder.build())
                playerRepository.fetchVideoPlaylist(videoId)
                        .map { response ->
                            val playlist = response.body()!!.string()
                            URL("https://.*\\.m3u8".toRegex().find(playlist)!!.value).openStream().use {
                                PlaylistParser(it, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                            }
                        }
                        .subscribe({
                            playlist = it
                            enqueueNextSegments(3)
                        }, {
                        })
            }
            is ClipRequest -> with(request as ClipRequest) {
                notificationBuilder.setContentText(offlineVideo.name)
                notificationManager.notify(offlineVideoId, notificationBuilder.build())
                val r = DownloadManager.Request(url.toUri()).apply {
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    setVisibleInDownloadsUi(false)
                    setDestinationUri(path)
                }
                downloadRequestId = downloadManager.enqueue(r)
            }
        }
        countDownLatch.await()
    }

    private fun enqueueNextSegments(count: Int) {
        with(request as VideoRequest) {
            var i = 0
            do {
                val track = playlist.tracks[currentTrack]
                val uri = Uri.withAppendedPath(path, track.trackInfo.title)
                if (uri.toFile().exists()) {
                    currentTrack++
                    continue
                }
                val r = DownloadManager.Request(track.uri.toUri()).apply {
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    setVisibleInDownloadsUi(false)
                    setDestinationUri(uri)
                }
                downloadRequestsToSegmentsMap.put(downloadManager.enqueue(r), currentTrack++)
                i++
            } while (i < count)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
        unregisterReceiver(downloadReceiver)
        when (request) {
            is VideoRequest -> {
                Log.d(TAG, "Downloaded video")
                with(request) {
                    val offlineTracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
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
                    var totalDuration = 0L
                    for (i in segmentFrom..segmentTo) {
                        val trackInfo = playlist.tracks[i].trackInfo
                        totalDuration += trackInfo.duration.toLong()
                        offlineTracks.add(TrackData.Builder()
                                .withUri(path.path!! + File.separator + trackInfo.title)
                                .withTrackInfo(TrackInfo(trackInfo.duration, trackInfo.title))
                                .build())
                    }
                    val mediaPlaylist = MediaPlaylist.Builder()
                            .withTargetDuration(playlist.targetDuration)
                            .withTracks(offlineTracks.toList())
                            .build()
                    val playlist = Playlist.Builder()
                            .withMediaPlaylist(mediaPlaylist)
                            .build()
                    val playlistPath = "${path.path!!}/$id.m3u8"
                    FileOutputStream(playlistPath).use {
                        val writer = PlaylistWriter(it, Format.EXT_M3U, Encoding.UTF_8)
                        writer.write(playlist)
                    }
                    Log.d(TAG, "Playlist created")
                }
            }
            is ClipRequest -> Log.d(TAG, "Downloaded clip")
        }
        val intent = Intent(this@with, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("video", offlineVideo)
        }
        notificationBuilder.apply {
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
            mActions.clear()
        }
        notificationManager.notify(offlineVideo.id, notificationBuilder.build())
        countDownLatch.countDown()
    }
}