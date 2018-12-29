package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.exact7.xtra.repository.OfflineRepository
import javax.inject.Inject

class DownloadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "DownloadWorker"
        private const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
        private const val CHANNEL_ID = "xtra_download_channel"
        const val REQUEST_VIDEO = 0
        const val REQUEST_CLIP = 1
        const val KEY_REQUEST_TYPE = "request"
        const val KEY_JSON_STRING = "json"
    }

    @Inject
    lateinit var repository: OfflineRepository
//    private val downloadReceiver: BroadcastReceiver
//    private val downloadManager: DownloadManager
//    private val notificationManager: NotificationManagerCompat
//    private var notificationBuilders = HashMap<Int, NotificationCompat.Builder>()
//    private val downloadRequestToRequestIdsMap = LongSparseArray<Int>()

    init {
//        downloadReceiver = object : BroadcastReceiver() {
//
//            override fun onReceive(context: Context, intent: Intent) {
//                val downloadRequestIndex = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
//                val requestId = downloadRequestToRequestIdsMap[downloadRequestIndex]
//                val request = map[requestId]
//                when (request) {
//                    is VideoRequest -> {
//                        with(request) {
//                            if (!request.canceled) {
//                                val segment = segments[downloadRequestToSegmentMap[downloadRequestIndex]]
//                                val trackDuration = segment.second
//                                totalDuration += trackDuration
//                                tracks.add(TrackData.Builder()
//                                        .withUri(directoryPath + File.separator + segment.first)
//                                        .withTrackInfo(TrackInfo(trackDuration.toFloat(), segment.first))
//                                        .build())
//                                val builder = notificationBuilders[requestId]!!
//                                if (++currentProgress < maxProgress) {
//                                    enqueueNextSegment(request)
//                                    builder.setProgress(maxProgress, currentProgress, false)
//                                } else {
//                                    onDownloadCompleted(request)
//                                }
//                                notificationManager.notify(requestId, builder.build())
//                            } else {
//                                if (++currentProgress == maxProgress) {
//                                    map.remove(requestId)
//                                    val directory = File(directoryPath)
//                                    if (directory.list().isEmpty()) {
//                                        directory.deleteVideo()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    is ClipRequest -> {
//                        map.remove(requestId)
//                        if (!request.canceled) {
//                            onDownloadCompleted(request)
//                        }
//                    }
//                }
//                downloadRequestToRequestIdsMap.remove(downloadRequestIndex)
//            }
//        }
//        with (context) {
//            downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            notificationManager = NotificationManagerCompat.from(this)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(CHANNEL_ID, getString(R.string.downloads_channel), NotificationManager.IMPORTANCE_LOW).apply {
//                    setSound(null, null)
//                }
//                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                manager.createNotificationChannel(channel)
//            }
//            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//        }
    }

    override fun doWork(): Result {
//        Log.d(TAG, "Starting download")
//        val request: Request = Gson().fromJson(
//                inputData.getString(KEY_JSON_STRING),
//                if (inputData.getInt(KEY_REQUEST_TYPE, -1) == REQUEST_VIDEO) {
//                    VideoRequest::class.java
//                } else {
//                    ClipRequest::class.java
//                })
//        println(request)
        val i = inputData.getInt("key", -1)
        println("start $i")
        Thread.sleep(5000)
        println("end $i")
        return Result.success()
//        with (applicationContext) {
//            val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
//                setSmallIcon(R.drawable.ic_notification)
//                priority = NotificationCompat.PRIORITY_LOW
//                setGroup(GROUP_KEY)
//                setContentTitle(getString(R.string.downloading))
//                setOngoing(true)
//                val cancelIntent = Intent(this@with, CancelActionReceiver::class.java).putExtra("requestId", request.id)
//                addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@with, request.id
//                        , cancelIntent, 0)))
//            }
//            when (request) {
//                is VideoRequest -> with(request) {
//                    getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.let {
//                        directoryUri = it.toUri()
//                        directoryPath = it.absolutePath
//                    }
//                    builder.apply {
//                        setContentText(video.title)
//                        setProgress(maxProgress, currentProgress, false)
//                    }
//                    val notification = builder.build()
//                    notificationManager.notify(id, notification)
//                    notificationBuilders[request.id] = builder
//                    enqueueNextSegment(request)
//                }
//                is ClipRequest -> with(request) {
//                    builder.setContentText(clip.title)
//                    val notification = builder.build()
//                    notificationManager.notify(id, notification)
//                    notificationBuilders[request.id] = builder
//                    val r = DownloadManager.Request(url.toUri()).apply {
//                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                        setVisibleInDownloadsUi(false)
//                        val downloadPath = getExternalFilesDir(".downloads" + File.separator + clip.slug + quality)!!
//                        setDestinationUri(downloadPath.toUri())
//                        path = downloadPath.absolutePath + ".mp4"
//                    }
//                    val requestId = downloadManager.enqueue(r)
//                    downloadRequestId = requestId
//                    downloadRequestToRequestIdsMap.put(requestId, id)
//                }
//            }
//        }
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
//        fun saveVideo(f: () -> OfflineVideo) {
//            GlobalScope.launch {
//                Log.d(TAG, "Saving video")
//                val video = f.invoke()
//                repository.saveVideo(video)
//                val intent = Intent(this@DownloadWorker, MainActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    putExtra("video", video)
//                }
//                val builder = notificationBuilders[request.id]!!
//                builder.apply {
//                    setAutoCancel(true)
//                    setContentTitle(getString(R.string.downloaded))
//                    setProgress(0, 0, false)
//                    setOngoing(false)
//                    setContentIntent(PendingIntent.getActivity(this@DownloadWorker, 0, intent, 0))
//                    mActions.clear()
//                }
//                notificationManager.notify(request.id, builder.build())
//            }
//        }
//        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
//        val glide = GlideApp.with(this)
//        when (request) {
//            is VideoRequest -> {
//                Log.d(TAG, "Downloaded video")
//                with(request) {
//                    val mediaPlaylist = MediaPlaylist.Builder()
//                            .withTargetDuration(targetDuration)
//                            .withTracks(tracks.toList())
//                            .build()
//                    val playlist = Playlist.Builder()
//                            .withMediaPlaylist(mediaPlaylist)
//                            .build()
//                    val playlistPath = directoryPath + "/${System.currentTimeMillis()}.m3u8"
//                    val out = FileOutputStream(playlistPath)
//                    val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
//                    writer.write(playlist)
//                    out.close()
//                    Log.d(TAG, "Playlist created")
//                    saveVideo {
//                        with(video) {
//                            val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
//                            val logo = glide.downloadOnly().load(channel.logo).submit().get().absolutePath
//                            OfflineVideo(playlistPath, title, channel.name, game, totalDuration, currentDate, createdAt, thumbnail, logo)
//                        }
//                    }
//                }
//            }
//            is ClipRequest -> {
//                Log.d(TAG, "Downloaded clip")
//                saveVideo {
//                    with (request.clip) {
//                        val thumbnail = glide.downloadOnly().load(thumbnails.medium).submit().get().absolutePath
//                        val logo = glide.downloadOnly().load(broadcaster.logo).submit().get().absolutePath
//                        OfflineVideo(request.path, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo)
//                    }
//                }
//            }
//        }
    }

//    override fun onDestroy() {
//        unregisterReceiver(downloadReceiver)
//        super.onDestroy()
//    }

//    private fun enqueueNextSegment(request: VideoRequest) {
//        with (request) {
//            val url = segments[currentProgress].first
//            val r = DownloadManager.Request((baseUrl + url).toUri()).apply {
//                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                setVisibleInDownloadsUi(false)
//                setDestinationUri(Uri.withAppendedPath(directoryUri, url))
//            }
//            val requestId = downloadManager.enqueue(r)
//            downloadRequestToSegmentMap.put(requestId, currentProgress)
//            downloadRequestToRequestIdsMap.put(requestId, id)
//        }
//    }
}

sealed class Request(val id: Int) {
    var canceled = false
}



//data class ClipRequest(
//        val clip: Clip,
//        val quality: String,
//        val url: String
//) : Request() {
//    override val id = clip.slug.hashCode()
//    lateinit var path: String
//}