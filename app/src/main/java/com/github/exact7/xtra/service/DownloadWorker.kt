package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.exact7.xtra.GlideApp
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
import com.tonyodev.fetch2.DefaultFetchNotificationManager
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.DownloadNotification
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class DownloadWorker @Inject constructor(
        application: Application,
        workerParams: WorkerParameters,
        private val repository: OfflineRepository) : Worker(application, workerParams) {

    companion object {
        const val TYPE_VIDEO = 0
        const val TYPE_CLIP = 1
        private const val TAG = "DownloadWorker"
        private const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
        private const val CHANNEL_ID = "xtra_download_channel"
        private var fetchConfiguration: FetchConfiguration? = null

        fun download(requestId: Int, type: Int) {
            val data = Data.Builder().putInt("id", requestId).putInt("type", type).build()
            val work = OneTimeWorkRequest.Builder(DownloadWorker::class.java).setInputData(data).build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    private val fetch: Fetch

    init {
        if (fetchConfiguration == null) {
            fetchConfiguration = FetchConfiguration.Builder(application)
                    .enableLogging(true)
                    .enableRetryOnNetworkGain(true)
                    .setDownloadConcurrentLimit(3)
                    .setNotificationManager(object : DefaultFetchNotificationManager(application) {
                        override fun cancelNotification(notificationId: Int) {
                            Log.d(TAG, "cancelNotification")
                            super.cancelNotification(notificationId)
                        }

                        override fun cancelOngoingNotifications() {
                            Log.d(TAG, "cancelOngoingNotifications")
                            super.cancelOngoingNotifications()
                        }

                        override fun createNotificationChannels(context: Context, notificationManager: NotificationManager) {
                            Log.d(TAG, "createNotificationChannels")
                            super.createNotificationChannels(context, notificationManager)
                        }

                        override fun getActionPendingIntent(downloadNotification: DownloadNotification, actionType: DownloadNotification.ActionType): PendingIntent {
                            Log.d(TAG, "getActionPendingIntent")
                            return super.getActionPendingIntent(downloadNotification, actionType)
                        }

                        override fun getChannelId(notificationId: Int, context: Context): String {
                            Log.d(TAG, "getChannelId")
                            return super.getChannelId(notificationId, context)
                        }

                        override fun getGroupActionPendingIntent(groupId: Int, downloadNotifications: List<DownloadNotification>, actionType: DownloadNotification.ActionType): PendingIntent {
                            Log.d(TAG, "getGroupActionPendingIntent")
                            return super.getGroupActionPendingIntent(groupId, downloadNotifications, actionType)
                        }

                        override fun getNotificationBuilder(notificationId: Int, groupId: Int): NotificationCompat.Builder {
                            Log.d(TAG, "getNotificationBuilder")
                            return super.getNotificationBuilder(notificationId, groupId)
                        }

                        override fun getOngoingDismissalDelay(notificationId: Int, groupId: Int): Long {
                            Log.d(TAG, "getOngoingDismissalDelay")
                            return super.getOngoingDismissalDelay(notificationId, groupId)
                        }

                        override fun handleNotificationOngoingDismissal(notificationId: Int, groupId: Int, ongoingNotification: Boolean) {
                            Log.d(TAG, "handleNotificationOngoingDismissal")
                            super.handleNotificationOngoingDismissal(notificationId, groupId, ongoingNotification)
                        }

                        override fun notify(groupId: Int) {
                            Log.d(TAG, "notify")
                            super.notify(groupId)
                        }

                        override fun updateGroupSummaryNotification(groupId: Int, notificationBuilder: NotificationCompat.Builder, downloadNotifications: List<DownloadNotification>, context: Context): Boolean {
                            Log.d(TAG, "updateGroupSummaryNotification")
                            return super.updateGroupSummaryNotification(groupId, notificationBuilder, downloadNotifications, context)
                        }

                        override fun updateNotification(notificationBuilder: NotificationCompat.Builder, downloadNotification: DownloadNotification, context: Context) {
                            Log.d(TAG, "updateNotification")
                            super.updateNotification(notificationBuilder, downloadNotification, context)
                        }
                    })
                    .build()
        }
        fetch = Fetch.getInstance(fetchConfiguration!!)
    }

    override fun doWork(): Result { //TODO Maybe create lock from this method?
        Log.d(TAG, "Starting download")
        val countDownLatch = CountDownLatch(1) //<----- USE THIS INSIDE METHOD POGCHAMP
        val requestId = inputData.getInt("id", -1)
        val request: com.github.exact7.xtra.model.offline.Request = runBlocking {
            if (inputData.getInt("type", -1) == TYPE_VIDEO) {
                repository.getVideoRequest(requestId)
            } else {
                repository.getClipRequest(requestId)
            }
        }
        when (request) {
            is VideoRequest -> with (request) {
                path = applicationContext.getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.absolutePath + "/"
                val requests = segments.map { (fileName, duration) -> com.tonyodev.fetch2.Request(baseUrl + fileName, path + fileName).also { totalDuration += duration } }
                fetch.addListener(object : FetchListener {

                    var downloaded = 0

                    override fun onAdded(download: Download) {
                    }

                    override fun onCancelled(download: Download) {
                    }

                    override fun onCompleted(download: Download) {
                        if (++downloaded == segments.size) {
                            GlobalScope.launch {
                                onDownloadCompleted(request) //TODO add code from downloadmanager receiver
                                countDownLatch.countDown()
                            }
                        }
                    }

                    override fun onDeleted(download: Download) {
                    }

                    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
                    }

                    override fun onError(download: Download, error: Error, throwable: Throwable?) {
                    }

                    override fun onPaused(download: Download) {
                    }

                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                    }

                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                    }

                    override fun onRemoved(download: Download) {
                    }

                    override fun onResumed(download: Download) {
                    }

                    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                    }

                    override fun onWaitingNetwork(download: Download) {
                    }
                })
                fetch.enqueue(requests, Func {

                })
            }
            is ClipRequest -> with(request) {
                path = applicationContext.getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.absolutePath + ".mp4"
                fetch.enqueue(com.tonyodev.fetch2.Request(url, path))
            }
        }
        countDownLatch.await()
        return Result.success()
    }
    ////        val request = queue.peek()
//    with (applicationContext) {
//        when (request) {
//            is VideoRequest -> with(request) { //TODO skip both video and clip if exist
//                getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.let {
//                    directoryUri = it.toUri()
//                    directory = it.absolutePath
//                }
//                notificationBuilder.apply {
//                    setContentText(video.title)
//                    setProgress(maxProgress, currentProgress, false)
//                }
//                notificationManager.notify(id, notificationBuilder.build())
//                enqueueNextSegment(request)
//            }
//            is ClipRequest -> with(request) {
//                notificationBuilder.setContentText(clip.title)
//                notificationManager.notify(id, notificationBuilder.build())
//                val r = DownloadManager.Request(url.toUri()).apply {
//                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                    setVisibleInDownloadsUi(false)
//                    getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.let {
//                        setDestinationUri(it.toUri())
//                        path = it.absolutePath + ".mp4"
//                    }
//                }
//                downloadRequestId = downloadManager.enqueue(r)
//            }
//        }
//    }
//    countDownLatch.await()
//    queue.remove()
//    return Result.success() //TODO create offline video before finish and assign it id of request to track progress in ui if canceled return id and delete it in viewmodel
//}
//
    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
        with(applicationContext) {
            //            unregisterReceiver(downloadReceiver)
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
                        val playlistPath = path + "${System.currentTimeMillis()}.m3u8"
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
//            notificationBuilder.apply {
//                setAutoCancel(true)
//                setContentTitle(getString(R.string.downloaded))
//                setProgress(0, 0, false)
//                setOngoing(false)
//                setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
//                mActions.clear()
//            }
//            notificationManager.notify(request.id, notificationBuilder.build())
        }
    }

    class CancelActionReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
//            GlobalScope.launch {
//                Log.d(TAG, "Canceled download")
//                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                val request = queue.peek()
//                notificationManager.cancel(request.id)
//                request.canceled = true
//                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                when (request) {
//                    is VideoRequest -> {
//                        val iterator = request.downloadRequestToSegmentMap.keyIterator()
//                        while (iterator.hasNext()) {
//                            downloadManager.remove(iterator.next())
//                        }
//                    }
//                    is ClipRequest -> downloadManager.remove(request.downloadRequestId)
//                }
//            }
        }
    }
}