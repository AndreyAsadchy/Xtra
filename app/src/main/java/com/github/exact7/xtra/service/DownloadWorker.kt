package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
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
import androidx.core.net.toUri
import androidx.core.util.keyIterator
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.TwitchApiHelper
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistWriter
import com.iheartradio.m3u8.data.MediaPlaylist
import com.iheartradio.m3u8.data.Playlist
import com.iheartradio.m3u8.data.TrackData
import com.iheartradio.m3u8.data.TrackInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class DownloadWorker @Inject constructor(
        application: Application,
        workerParams: WorkerParameters,
        private val repository: OfflineRepository) : Worker(application, workerParams) {

    companion object {
        private const val TAG = "DownloadWorker"
        private const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
        private const val CHANNEL_ID = "xtra_download_channel"
        private val queue: Queue<Request> = LinkedList()
        private var isNotificationChannelCreated = false

        fun download(request: Request) {
            queue.add(request)
            val work = OneTimeWorkRequest.Builder(DownloadWorker::class.java).build()
            WorkManager.getInstance().enqueueUniqueWork(TAG, ExistingWorkPolicy.APPEND, work)
        }
    }

    private val downloadReceiver: BroadcastReceiver
    private val downloadManager: DownloadManager
    private val notificationBuilder: NotificationCompat.Builder
    private val notificationManager: NotificationManagerCompat
    private val downloadHandler: Handler
    private val countDownLatch = CountDownLatch(1)

    init {
        with (application) {
            downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_notification)
                setGroup(GROUP_KEY)
                setContentTitle(getString(R.string.downloading))
                setOngoing(true)
                val cancelIntent = Intent(this@with, CancelActionReceiver::class.java)
                addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@with, 0, cancelIntent, 0)))
            }
            notificationManager = NotificationManagerCompat.from(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNotificationChannelCreated) {
                val channel = NotificationChannel(CHANNEL_ID, getString(R.string.downloads_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                }
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
                isNotificationChannelCreated = true
            }
        }
        downloadReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val downloadRequestIndex = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                val request = queue.peek()
                when (request) {
                    is VideoRequest -> {
                        with(request) {
                            if (!canceled) {
                                val segment = segments[downloadRequestToSegmentMap[downloadRequestIndex]]
                                val trackDuration = segment.second
                                totalDuration += trackDuration
                                tracks.add(TrackData.Builder()
                                        .withUri(directoryPath + File.separator + segment.first)
                                        .withTrackInfo(TrackInfo(trackDuration.toFloat(), segment.first))
                                        .build())
                                if (++currentProgress < maxProgress) {
                                    enqueueNextSegment(this)
                                    notificationBuilder.setProgress(maxProgress, currentProgress, false)
                                    notificationManager.notify(id, notificationBuilder.build())
                                } else {
                                    onDownloadCompleted(this)
                                }
                            } else {
                                if (!deleted) {
                                    deleted = true
                                    val directory = File(directoryPath)
                                    if (directory.exists() && directory.list().isEmpty()) {
                                        directory.deleteRecursively()
                                    }
                                    countDownLatch.countDown()
                                }
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
        downloadHandler = HandlerThread("RequestThread", Process.THREAD_PRIORITY_BACKGROUND).run {
            start()
            Handler(looper)
        }
        application.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), null, downloadHandler)
    }

    override fun doWork(): Result {
        //TODO save request to database so if app closed we can retreive it and retry
        Log.d(TAG, "Starting download")
        val request = queue.peek()
        with (applicationContext) {
            when (request) {
                is VideoRequest -> with(request) {
                    getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.let {
                        directoryUri = it.toUri()
                        directoryPath = it.absolutePath
                    }
                    notificationBuilder.apply {
                        setContentText(video.title)
                        setProgress(maxProgress, currentProgress, false)
                    }
                    notificationManager.notify(id, notificationBuilder.build())
                    enqueueNextSegment(request)
                }
                is ClipRequest -> with(request) {
                    notificationBuilder.setContentText(clip.title)
                    notificationManager.notify(id, notificationBuilder.build())
                    val r = DownloadManager.Request(url.toUri()).apply {
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                        setVisibleInDownloadsUi(false)
                        getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.let {
                            setDestinationUri(it.toUri())
                            path = it.absolutePath + ".mp4"
                        }
                    }
                    downloadRequestId = downloadManager.enqueue(r)
                }
            }
        }
        countDownLatch.await()
        queue.remove()
        return Result.success() //TODO create offline video before finish and assign it id of request to track progress in ui if canceled return id and delete it in viewmodel
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
        with(applicationContext) {
            unregisterReceiver(downloadReceiver)
            val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
            val glide = GlideApp.with(this)
            val video: OfflineVideo = when (request) {
                is VideoRequest -> {
                    Log.d(TAG, "Downloaded video")
                    with(request) {
                        val mediaPlaylist = MediaPlaylist.Builder()
                                .withTargetDuration(targetDuration)
                                .withTracks(tracks.toList())
                                .build()
                        val playlist = Playlist.Builder()
                                .withMediaPlaylist(mediaPlaylist)
                                .build()
                        val playlistPath = directoryPath + "/${System.currentTimeMillis()}.m3u8"
                        val out = FileOutputStream(playlistPath)
                        val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
                        writer.write(playlist)
                        out.close()
                        Log.d(TAG, "Playlist created")
                        with(video) {
                            val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
                            val logo = glide.downloadOnly().load(channel.logo).submit().get().absolutePath
                            OfflineVideo(playlistPath, title, channel.name, game, totalDuration, currentDate, createdAt, thumbnail, logo)
                        }
                    }
                }
                is ClipRequest -> {
                    Log.d(TAG, "Downloaded clip")
                    with(request.clip) {
                        val thumbnail = glide.downloadOnly().load(thumbnails.medium).submit().get().absolutePath
                        val logo = glide.downloadOnly().load(broadcaster.logo).submit().get().absolutePath
                        OfflineVideo(request.path, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo)
                    }
                }
            }
            Log.d(TAG, "Saving video")
            repository.saveVideo(video)
            val intent = Intent(this@with, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("video", video)
            }
            notificationBuilder.apply {
                setAutoCancel(true)
                setContentTitle(getString(R.string.downloaded))
                setProgress(0, 0, false)
                setOngoing(false)
                setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
                mActions.clear()
            }
            notificationManager.notify(request.id, notificationBuilder.build())
            countDownLatch.countDown()
        }
    }

    private fun enqueueNextSegment(request: VideoRequest) {
        with (request) {
            val url = segments[currentProgress].first
            val r = DownloadManager.Request((baseUrl + url).toUri()).apply {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setVisibleInDownloadsUi(false)
                setDestinationUri(Uri.withAppendedPath(directoryUri, url))
            }
            downloadRequestToSegmentMap.put(downloadManager.enqueue(r), currentProgress)
        }
    }

    class CancelActionReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            GlobalScope.launch {
                Log.d(TAG, "Canceled download")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val request = queue.peek()
                notificationManager.cancel(request.id)
                request.canceled = true
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                when (request) {
                    is VideoRequest -> {
                        val iterator = request.downloadRequestToSegmentMap.keyIterator()
                        while (iterator.hasNext()) {
                            downloadManager.remove(iterator.next())
                        }
                    }
                    is ClipRequest -> downloadManager.remove(request.downloadRequestId)
                }
            }
        }
    }
}