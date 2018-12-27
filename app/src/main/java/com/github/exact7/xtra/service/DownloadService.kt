package com.github.exact7.xtra.service

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.util.keyIterator
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.OfflineVideo
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
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
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.Comparator
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.set

class DownloadService : Service() {

    companion object {
        private const val TAG = "DownloadService"
        private const val GROUP_KEY = "com.github.exact7.xtra.DOWNLOADS"
        private const val CHANNEL_ID = "xtra_download_channel"
        private val queue: Queue<Request> = LinkedList()
        private val downloadRequestToRequestIdsMap = LongSparseArray<Int>()
        private val map = HashMap<Int, Request>()

        fun download(context: Context, request: Request) {
            request.let {
                queue.add(it)
                map[it.id] = it
            }
            val intent = Intent(context, DownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    @Inject
    lateinit var repository: OfflineRepository

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManagerCompat
    private var notificationBuilders = HashMap<Int, NotificationCompat.Builder>()
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val downloadRequestIndex = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            val requestId = downloadRequestToRequestIdsMap[downloadRequestIndex]
            val request = map[requestId]
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
                            val builder = notificationBuilders[requestId]!!
                            if (++currentProgress < maxProgress) {
                                enqueueNextSegment(request)
                                builder.setProgress(maxProgress, currentProgress, false)
                            } else {
                                onDownloadCompleted(request)
                            }
                            notificationManager.notify(requestId, builder.build())
                        } else {
                            if (++currentProgress == maxProgress) {
                                map.remove(requestId)
                                val directory = File(directoryPath)
                                if (directory.list().isEmpty()) {
                                    directory.delete()
                                }
                            }
                        }
                    }
                }
                is ClipRequest -> {
                    map.remove(requestId)
                    if (!request.canceled) {
                        onDownloadCompleted(request)
                    }
                }
            }
            downloadRequestToRequestIdsMap.remove(downloadRequestIndex)
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this@DownloadService)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = NotificationManagerCompat.from(this@DownloadService)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.downloads_channel), NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting download")
        val request = queue.poll()
        val builder = NotificationCompat.Builder(this@DownloadService, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            priority = NotificationCompat.PRIORITY_LOW
            setGroup(GROUP_KEY)
            setContentTitle(getString(R.string.downloading))
            setOngoing(true)
            val cancelIntent = Intent(this@DownloadService, CancelReceiver::class.java).putExtra("requestId", request.id)
            addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, request.id
                    , cancelIntent, 0)))
        }
        when (request) {
            is VideoRequest -> with(request) {
                getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.let {
                    directoryUri = it.toUri()
                    directoryPath = it.absolutePath
                }
                builder.apply {
                    setContentText(video.title)
                    setProgress(maxProgress, currentProgress, false)
                }
                val notification = builder.build()
                notificationManager.notify(id, notification)
                notificationBuilders[request.id] = builder
                startForeground(id, notification)
                enqueueNextSegment(request)
            }
            is ClipRequest -> with(request) {
                builder.setContentText(clip.title)
                val notification = builder.build()
                notificationManager.notify(id, notification)
                notificationBuilders[request.id] = builder
                startForeground(id, notification)
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
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted(request: Request) {
        fun saveVideo(f: () -> OfflineVideo) {
            GlobalScope.launch {
                Log.d(TAG, "Saving video")
                val video = f.invoke()
                repository.insert(video)
                val intent = Intent(this@DownloadService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("video", video)
                }
                val builder = notificationBuilders[request.id]!!
                builder.apply {
                    setAutoCancel(true)
                    setContentTitle(getString(R.string.downloaded))
                    setProgress(0, 0, false)
                    setOngoing(false)
                    setContentIntent(PendingIntent.getActivity(this@DownloadService, 0, intent, 0))
                    mActions.clear()
                }
                notificationManager.notify(request.id, builder.build())
            }
        }
        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
        val glide = GlideApp.with(this)
        when (request) {
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
                    saveVideo {
                        with(video) {
                            val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
                            val logo = glide.downloadOnly().load(channel.logo).submit().get().absolutePath
                            OfflineVideo(playlistPath, title, channel.name, game, totalDuration, currentDate, createdAt, thumbnail, logo)
                        }
                    }
                }
            }
            is ClipRequest -> {
                Log.d(TAG, "Downloaded clip")
                saveVideo {
                    with (request.clip) {
                        val thumbnail = glide.downloadOnly().load(thumbnails.medium).submit().get().absolutePath
                        val logo = glide.downloadOnly().load(broadcaster.logo).submit().get().absolutePath
                        OfflineVideo(request.path, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
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

    override fun onBind(intent: Intent?): IBinder? = null

    class CancelReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Canceled download")
            val requestId = intent.getIntExtra("requestId", 0)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(requestId)
            val request = map[requestId]!!
            request.canceled = true
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            when (request) {
                is VideoRequest -> {
                    with(request) {
                        val iterator = downloadRequestToSegmentMap.keyIterator()
                        while (iterator.hasNext()) {
                            downloadManager.remove(iterator.next())
                        }
                    }
                }
                is ClipRequest -> downloadManager.remove(request.downloadRequestId)
            }
        }
    }
}

sealed class Request {
    abstract val id: Int
    var canceled = false
}

data class VideoRequest(
        val video: Video,
        val quality: String,
        val baseUrl: String,
        val segments: ArrayList<Pair<String, Long>>,
        val targetDuration: Int
) : Request() {

    override val id = video.id.substring(1).toInt()
    val maxProgress = segments.size
    var currentProgress = 0
    var totalDuration = 0L
    lateinit var directoryUri: Uri
    lateinit var directoryPath: String
    val downloadRequestToSegmentMap = LongSparseArray<Int>()
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
}

data class ClipRequest(
        val clip: Clip,
        val quality: String,
        val url: String
) : Request() {
    override val id = clip.slug.hashCode()
    var downloadRequestId = 0L
    lateinit var path: String
}