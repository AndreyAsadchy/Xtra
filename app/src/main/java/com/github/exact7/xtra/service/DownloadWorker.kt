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
import android.util.LongSparseArray
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.util.keyIterator
import androidx.work.Data
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
        private val downloadRequestToRequestIdsMap = LongSparseArray<Int>()
        private val requests = SparseArray<Request>()
        private val notificationBuilders = SparseArray<NotificationCompat.Builder>()
        private var isNotificationChannelCreated = false

        fun download(request: Request) {
            val data = Data.Builder().putInt("key", request.id).build()
            val work = OneTimeWorkRequest.Builder(DownloadWorker::class.java).setInputData(data).build()
            request.workId = work.id
            requests.put(request.id, request)
            WorkManager.getInstance().enqueue(work)
        }
    }

    private val downloadReceiver: BroadcastReceiver
    private val downloadHandler: Handler
    private val downloadManager: DownloadManager
    private val notificationManager: NotificationManagerCompat
    private val countDownLatch = CountDownLatch(1)
    private lateinit var result: Result

    init {
        with (application) {
            downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
                println(downloadRequestToRequestIdsMap)
                val requestId = downloadRequestToRequestIdsMap[downloadRequestIndex]
                val request = requests[requestId]
                when (request) {
                    is VideoRequest -> {
                        with(request) {
                            if (!request.canceled) {
                                val segment = segments[downloadRequestToSegmentMap[downloadRequestIndex]]
                                val trackDuration = segment.second
                                totalDuration += trackDuration
                                tracks.add(TrackData.Builder()
                                        .withUri(directoryPath + File.separator + segment.first)
                                        .withTrackInfo(TrackInfo(trackDuration.toFloat(), segment.first))
                                        .build())
                                if (++currentProgress < maxProgress) {
                                    enqueueNextSegment(request)
                                    val notificationBuilder = notificationBuilders[requestId].setProgress(maxProgress, currentProgress, false)
                                    notificationManager.notify(requestId, notificationBuilder.build())
                                } else {
                                    onDownloadCompleted(request)
                                }
                            } else {
                                println("CANCEL $requestId")
                                if (!request.deleted) {
                                    val directory = File(directoryPath)
                                    if (directory.list().isEmpty()) {
                                        directory.delete()
                                    }
                                    result = Result.failure()
                                    countDownLatch.countDown()
                                    request.deleted = true
                                }
                            }
                        }
                    }
                    is ClipRequest -> {
                        if (!request.canceled) {
                            onDownloadCompleted(request)
                        } else {
                            result = Result.failure()
                            countDownLatch.countDown()
                        }
                    }
                }
            }
        }
        downloadHandler = HandlerThread("RequestThread #${requests.size()}", Process.THREAD_PRIORITY_BACKGROUND).run {
            start()
            Handler(looper)
        }
        application.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), null, downloadHandler)
    }

    override fun doWork(): Result {
        println("start $downloadReceiver $this $countDownLatch $downloadHandler")
        Log.d(TAG, "Starting download")
        val request = requests[inputData.getInt("key", -1)]
        with (applicationContext) {
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_notification)
                setGroup(GROUP_KEY)
                setContentTitle(getString(R.string.downloading))
                setOngoing(true)
                val cancelIntent = Intent(this@with, CancelActionReceiver::class.java).putExtra("requestId", request.id)
                addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@with, request.id
                        , cancelIntent, 0)))
            }
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
                    notificationBuilders.put(id, notificationBuilder)
                    enqueueNextSegment(request)
                }
                is ClipRequest -> with(request) {
                    notificationBuilder.setContentText(clip.title)
                    notificationManager.notify(id, notificationBuilder.build())
                    notificationBuilders.put(id, notificationBuilder)
                    val r = DownloadManager.Request(url.toUri()).apply {
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                        setVisibleInDownloadsUi(false)
                        val downloadPath = getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!
                        setDestinationUri(downloadPath.toUri())
                        path = downloadPath.absolutePath + ".mp4"
                    }
                    val requestId = downloadManager.enqueue(r)
                    downloadRequestId = requestId
                    downloadRequestToRequestIdsMap.put(requestId, id)
                }
            }
        }
        while (!request.canceled) {

        }
//        countDownLatch.await()
        result = Result.success()
        println("stop $this $result $countDownLatch $downloadHandler")
        return result
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
        with(applicationContext) {
            if (requests.size() == 0)
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
            val notificationBuilder = notificationBuilders[request.id].apply {
                setAutoCancel(true)
                setContentTitle(getString(R.string.downloaded))
                setProgress(0, 0, false)
                setOngoing(false)
                setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
                mActions.clear()
            }
            notificationManager.notify(request.id, notificationBuilder.build())
            result = Result.success()
            request.canceled = true
//            countDownLatch.countDown()
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
            val requestId = downloadManager.enqueue(r)
            downloadRequestToSegmentMap.put(requestId, currentProgress)
            downloadRequestToRequestIdsMap.put(requestId, id)
        }
    }

    class CancelActionReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            GlobalScope.launch {
                Log.d(TAG, "Canceled download")
                val requestId = intent.getIntExtra("requestId", 0)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(requestId)
                val request = requests[requestId]!!
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