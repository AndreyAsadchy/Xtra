package com.github.exact7.xtra.service

import android.app.Application
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.exact7.xtra.repository.OfflineRepository
import javax.inject.Inject

class DownloadWorker @Inject constructor(
        application: Application,
        workerParams: WorkerParameters,
        private val repository: OfflineRepository) : Worker(application, workerParams) {

    companion object {
        private const val TAG = "DownloadWorker"
        private const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"

        fun download(requestJson: String, isVideoRequest: Boolean) {
//            val data = Data.Builder().putInt("id", requestId).putInt("type", type).build()
//            val work = OneTimeWorkRequest.Builder(DownloadWorker::class.java).setInputData(data).build()
//            WorkManager.getInstance().enqueueUniqueWork(TAG, ExistingWorkPolicy.APPEND, work)
        }
        //TODO maybe pass only url and from and to indexes?
    }

//    private val downloadReceiver: BroadcastReceiver
//    private val downloadManager: DownloadManager
//    private val notificationBuilder: NotificationCompat.Builder
//    private val notificationManager: NotificationManagerCompat
//    private val downloadHandler: Handler
//
//    init {
//        with(application) {
//            downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            val channelId = getString(R.string.notification_channel_id)
//            notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
//                setSmallIcon(R.drawable.ic_notification)
//                setGroup(GROUP_KEY)
//                setContentTitle(getString(R.string.downloading))
//                setOngoing(true)
//                val cancelIntent = Intent(this@with, CancelActionReceiver::class.java)
//                addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@with, 0, cancelIntent, 0)))
//            }
//            notificationManager = NotificationManagerCompat.from(this)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                var channelName = manager.getNotificationChannel(channelId)
//                if (channelName == null) {
//                    channelName = NotificationChannel(channelId, getString(R.string.notification_downloads_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
//                        setSound(null, null)
//                        manager.createNotificationChannel(channelName)
//                    }
//
//                }
//            }
//            downloadReceiver = object : BroadcastReceiver() {
//
//                override fun onReceive(context: Context, intent: Intent) {
//                    val downloadRequestIndex = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
//                    val request = queue.peek()
//                    when (request) {
//                        is VideoRequest -> {
//                            with(request) {
//                                if (!canceled) {
//                                    val segment = segments[downloadRequestToSegmentMap[downloadRequestIndex]]
//                                    tracks.add(TrackData.Builder()
//                                            .withUri(directoryPath + File.separator + segment.first)
//                                            .withTrackInfo(TrackInfo(segment.second.toFloat(), segment.first))
//                                            .build())
//                                    if (++currentProgress < maxProgress) {
//                                        enqueueNextSegment(this)
//                                        notificationBuilder.setProgress(maxProgress, currentProgress, false)
//                                        notificationManager.notify(id, notificationBuilder.build())
//                                    } else {
//                                        onDownloadCompleted(this)
//                                    }
//                                } else {
//                                    if (!deleted) {
//                                        deleted = true
//                                        val directory = File(directoryPath)
//                                        if (directory.exists() && directory.list().isEmpty()) {
//                                            directory.deleteRecursively()
//                                        }
//                                        countDownLatch.countDown()
//                                    }
//                                }
//                            }
//                        }
//                        is ClipRequest -> {
//                            if (!request.canceled) {
//                                onDownloadCompleted(request)
//                            } else {
//                                countDownLatch.countDown()
//                            }
//                        }
//                    }
//                }
//            }
//            downloadHandler = HandlerThread("RequestThread", Process.THREAD_PRIORITY_BACKGROUND).run {
//                start()
//                Handler(looper)
//            }
//            application.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), null, downloadHandler)
//        }
//    }
//
//    class DownloadCompleteReceiver : BroadcastReceiver() {
//
//        override fun onReceive(context: Context?, intent: Intent?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//    }

    override fun doWork(): Result {
        Log.d(TAG, "Starting download")
        Thread.sleep(10000)
        Log.d(TAG, "Finish download")
//        DownloadManager.COLUMN_ //TODO maybe dont process file one by one and only after all downloaded in downloadmanager
//        with(applicationContext) {
//            val requestId = inputData.getInt("id", -1)
//            val request: Request = runBlocking {
//                if (inputData.getInt("type", -1) == TYPE_VIDEO) {
//                    repository.getVideoRequest(requestId)
//                } else {
//                    repository.getClipRequest(requestId)
//                }
//            }
//            when (request) {
//                is VideoRequest -> with(request) {
//                    getExternalFilesDir(".downloads" + File.separator + media_item.id + quality)!!.let {
//                        directoryUri = it.toUri()
//                        directoryPath = it.absolutePath
//                    }
//                    notificationBuilder.apply {
//                        setContentText(media_item.title)
//                        setProgress(maxProgress, currentProgress, false)
//                    }
//                    notificationManager.notify(id, notificationBuilder.build())
//                    enqueueNextSegment(request)
//                }
//                is ClipRequest -> with(request) {
//                    notificationBuilder.setContentText(clip.title)
//                    notificationManager.notify(id, notificationBuilder.build())
//                    val r = DownloadManager.Request(url.toUri()).apply {
//                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                        setVisibleInDownloadsUi(false)
//                        getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.let {
//                            setDestinationUri(it.toUri())
//                            path = it.absolutePath + ".mp4"
//                        }
//                    }
//                    downloadRequestId = downloadManager.enqueue(r)
//                }
//            }
//        }
//        countDownLatch.await()
//        queue.remove()
        return Result.success() //TODO create offline media_item before finish and assign it id of request to track progress in ui if canceled return id and delete it in viewmodel
    }
}
//
//    @SuppressLint("RestrictedApi")
//    private fun onDownloadCompleted(request: Request) {
//        with(applicationContext) {
//            unregisterReceiver(downloadReceiver)
//            val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
//            val glide = GlideApp.with(this)
//            val media_item: OfflineVideo = when (request) {
//                is VideoRequest -> {
//                    Log.d(TAG, "Downloaded media_item")
//                    with(request) {
//                        val mediaPlaylist = MediaPlaylist.Builder()
//                                .withTargetDuration(targetDuration)
//                                .withTracks(tracks.toList())
//                                .build()
//                        val playlist = Playlist.Builder()
//                                .withMediaPlaylist(mediaPlaylist)
//                                .build()
//                        val playlistPath = directoryPath + "/${System.currentTimeMillis()}.m3u8"
//                        val out = FileOutputStream(playlistPath)
//                        val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
//                        writer.write(playlist)
//                        out.close()
//                        Log.d(TAG, "Playlist created")
//                        with(media_item) {
//                            val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
//                            val logo = glide.downloadOnly().load(channelName.logo).submit().get().absolutePath
//                            OfflineVideo(playlistPath, title, channelName.name, game, totalDuration, currentDate, createdAt, thumbnail, logo)
//                        }
//                    }
//                }
//                is ClipRequest -> {
//                    Log.d(TAG, "Downloaded clip")
//                    with(request.clip) {
//                        val thumbnail = glide.downloadOnly().load(thumbnails.medium).submit().get().absolutePath
//                        val logo = glide.downloadOnly().load(broadcaster.logo).submit().get().absolutePath
//                        OfflineVideo(request.path, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo)
//                    }
//                }
//            }
//            Log.d(TAG, "Saving media_item")
//            repository.saveVideo(media_item)
//            val intent = Intent(this@with, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                putExtra("media_item", media_item)
//            }
//            notificationBuilder.apply {
//                setAutoCancel(true)
//                setContentTitle(getString(R.string.downloaded))
//                setProgress(0, 0, false)
//                setOngoing(false)
//                setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
//                mActions.clear()
//            }
//            notificationManager.notify(request.id, notificationBuilder.build())
//            countDownLatch.countDown()
//        }
//    }
//
//    private fun enqueueNextSegment(request: VideoRequest) {
//        with (request) {
//            val url = segments[currentProgress].first
//            val r = DownloadManager.Request((baseUrl + url).toUri()).apply {
//                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                setVisibleInDownloadsUi(false)
//                setDestinationUri(Uri.withAppendedPath(directoryUri, url))
//            }
//            downloadRequestToSegmentMap.put(downloadManager.enqueue(r), currentProgress)
//        }
//    }
//
//}
//
//override fun doWork(): Result { //TODO Maybe create lock from this method?
//    with(applicationContext) {
//
//        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
//        notificationBuilder.apply {
//            setSmallIcon(R.drawable.ic_notification)
//            setContentText(System.currentTimeMillis().toString())
//            val cancelIntent = Intent(this@with, CancelActionReceiver::class.java)
//            addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@with, 0, cancelIntent, 0)))
//        }
//        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
//    }
////        Log.d(TAG, "Starting download")
////        val countDownLatch = CountDownLatch(1) //<----- USE THIS INSIDE METHOD POGCHAMP
//
////        when (request) {
////            is VideoRequest -> with (request) {
////                path = applicationContext.getExternalFilesDir(".downloads" + File.separator + media_item.id + quality)!!.absolutePath + "/"
////                val extras = mapOf("id" to id.toString(), "name" to request.media_item.title, "size" to segments.size.toString())
////                val requests = segments.map { (fileName, _) -> com.tonyodev.fetch2.Request(baseUrl + fileName, path + fileName)
////                        .also { it.extras = Extras(extras) }
////                }
////                fetch.addListener(object : FetchListener {
////
////                    var downloaded = 0
////
////                    override fun onAdded(download: Download) {
////                    }
////
////                    override fun onCancelled(download: Download) {
////                    }
////
////                    override fun onCompleted(download: Download) {
////                        if (++downloaded == segments.size) {
////                            GlobalScope.launch {
////                                onDownloadCompleted(request) //TODO add code from downloadmanager receiver
////                                countDownLatch.countDown()
////                            }
////                        }
////                    }
////
////                    override fun onDeleted(download: Download) {
////                    }
////
////                    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
////                    }
////
////                    override fun onError(download: Download, error: Error, throwable: Throwable?) {
////                    }
////
////                    override fun onPaused(download: Download) {
////                    }
////
////                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
////                    }
////
////                    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
////                    }
////
////                    override fun onRemoved(download: Download) {
////                    }
////
////                    override fun onResumed(download: Download) {
////                    }
////
////                    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
////                    }
////
////                    override fun onWaitingNetwork(download: Download) {
////                    }
////                })
////                fetch.enqueue(requests, Func {
////
////                })
////            }
////            is ClipRequest -> with(request) {
////                path = applicationContext.getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.absolutePath + ".mp4"
////                fetch.enqueue(com.tonyodev.fetch2.Request(url, path))
////            }
////        }
////        countDownLatch.await()
//    println(Thread.currentThread().name)
//    Thread.sleep(10000)
//    return Result.success()
//}
//
//
//////        val request = queue.peek()
////    with (applicationContext) {
////        when (request) {
////            is VideoRequest -> with(request) { //TODO skip both media_item and clip if exist
////                getExternalFilesDir(".downloads" + File.separator + media_item.id + quality)!!.let {
////                    directoryUri = it.toUri()
////                    directory = it.absolutePath
////                }
////                notificationBuilder.apply {
////                    setContentText(media_item.title)
////                    setProgress(maxProgress, currentProgress, false)
////                }
////                notificationManager.notify(id, notificationBuilder.build())
////                enqueueNextSegment(request)
////            }
////            is ClipRequest -> with(request) {
////                notificationBuilder.setContentText(clip.title)
////                notificationManager.notify(id, notificationBuilder.build())
////                val r = DownloadManager.Request(url.toUri()).apply {
////                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
////                    setVisibleInDownloadsUi(false)
////                    getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!.let {
////                        setDestinationUri(it.toUri())
////                        path = it.absolutePath + ".mp4"
////                    }
////                }
////                downloadRequestId = downloadManager.enqueue(r)
////            }
////        }
////    }
////    countDownLatch.await()
////    queue.remove()
////    return Result.success() //TODO create offline media_item before finish and assign it id of request to track progress in ui if canceled return id and delete it in viewmodel
////}
////
//@SuppressLint("RestrictedApi")
//private fun onDownloadCompleted(request: Request) {
//    with(applicationContext) {
//        //            unregisterReceiver(downloadReceiver)
//        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
//        val glide = GlideApp.with(this)
//        val media_item: OfflineVideo = when (request) {
//            is VideoRequest -> {
//                Log.d(TAG, "Downloaded media_item")
//                with(request) {
//                    val mediaPlaylist = MediaPlaylist.Builder()
//                            .withTargetDuration(targetDuration)
//                            .withTracks(tracks.toList())
//                            .build()
//                    val playlist = Playlist.Builder()
//                            .withMediaPlaylist(mediaPlaylist)
//                            .build()
//                    val playlistPath = path + "${System.currentTimeMillis()}.m3u8"
//                    val out = FileOutputStream(playlistPath)
//                    val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
//                    writer.write(playlist)
//                    out.close()
//                    Log.d(TAG, "Playlist created")
//                    with(media_item) {
//                        val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
//                        val logo = glide.downloadOnly().load(channelName.logo).submit().get().absolutePath
//                        OfflineVideo(playlistPath, title, channelName.name, game, totalDuration, currentDate, createdAt, thumbnail, logo)
//                    }
//                }
//            }
//            is ClipRequest -> {
//                Log.d(TAG, "Downloaded clip")
//                with(request.clip) {
//                    val thumbnail = glide.downloadOnly().load(thumbnails.medium).submit().get().absolutePath
//                    val logo = glide.downloadOnly().load(broadcaster.logo).submit().get().absolutePath
//                    OfflineVideo(request.path, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo)
//                }
//            }
//        }
//        Log.d(TAG, "Saving media_item")
//        repository.saveVideo(media_item)
//        val intent = Intent(this@with, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            putExtra("media_item", media_item)
//        }
////            notificationBuilder.apply {
////                setAutoCancel(true)
////                setContentTitle(getString(R.string.downloaded))
////                setProgress(0, 0, false)
////                setOngoing(false)
////                setContentIntent(PendingIntent.getActivity(this@with, 0, intent, 0))
////                mActions.clear()
////            }
////            notificationManager.notify(request.id, notificationBuilder.build())
//    }
//}
//
//override fun onStopped() {
//    super.onStopped()
//}
//}