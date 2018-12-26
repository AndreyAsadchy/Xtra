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
import androidx.core.util.contains
import androidx.core.util.keyIterator
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.OfflineRepository
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
        private val requestToIdsMap = LongSparseArray<Int>()
        private val map = HashMap<Int, Request>()

        fun download(context: Context, request: Request) {
            request.let {
                queue.add(it)
                map[it.id] = it
            }
            context.startService(Intent(context, DownloadService::class.java))
        }
    }

    @Inject
    lateinit var repository: OfflineRepository

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManagerCompat
    private var notificationBuilders = HashMap<Int, NotificationCompat.Builder>()

    private val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
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

    private val receiver = object : BroadcastReceiver() {

        fun process(intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val segmentIndex = requestToIdsMap.get(id)
            val segment = segments[segmentIndex]
            val trackDuration = segment.second
            totalDuration += trackDuration
            tracks.add(TrackData.Builder()
                    .withUri(directoryPath + File.separator + segment.first)
                    .withTrackInfo(TrackInfo(trackDuration.toFloat(), segment.first))
                    .build())
        }

        @SuppressLint("RestrictedApi")
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                if (!requestToIdsMap.contains(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1))) return //TODO maybe do one download service?
                processedCount++
                if (!canceled) {
                    if (++currentProgress != maxProgress) {
                        enqueueNext()
                        process(intent)
                        notificationBuilder.setProgress(maxProgress, currentProgress, false)
                        notificationManager.notify(notificationId, notificationBuilder.build())
                    } else {
                        process(intent)
                        onDownloadCompleted()
                    }
                } else {
                    if (processedCount == currentProgress + 1) {
                        Log.d(TAG, "Canceled download")
                        notificationManager.cancel(notificationId)
                        canceled = false
                        val directory = File(directoryPath)
                        if (directory.list().isEmpty()) {
                            directory.delete()
                        }
                        reset()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = NotificationManagerCompat.from(this)
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
        GlobalScope.launch {
            val request = queue.poll()
            if (request is VideoRequest) {
                with(request) {
                    getExternalFilesDir(".downloads" + File.separator + video.id + quality)!!.let {
                        directoryUri = it.toUri()
                        directoryPath = it.absolutePath
                    }
                    val notification = NotificationCompat.Builder(this@DownloadService, CHANNEL_ID).apply {
                        setSmallIcon(R.drawable.ic_notification)
                        priority = NotificationCompat.PRIORITY_LOW
                        setGroup(GROUP_KEY)
                        setContentTitle(getString(R.string.downloading))
                        setContentText(video.title)
                        setOngoing(true)
                        setProgress(maxProgress, currentProgress, false)
                        addAction(NotificationCompat.Action(0, getString(R.string.cancel), PendingIntent.getBroadcast(this@DownloadService, id, Intent(this@DownloadService, CancelReceiver::class.java), 0)))
                    }

                    Log.d(TAG, "Starting download")
                    notificationManager.notify(id, notification.build())
                    for (segment in segments) {
                        val r = DownloadManager.Request((url + segment.first).toUri()).apply {
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                            setVisibleInDownloadsUi(false)
                            setDestinationUri(Uri.withAppendedPath(directoryUri, segment.first))
                        }
                        requestToIdsMap.put(downloadManager.enqueue(r), currentProgress)
                    }
                }
            } else {

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun onDownloadCompleted() {
        Log.d(TAG, "Downloading done. Creating playlist")
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
        Log.d(TAG, "Playlist created. Saving video")
        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this)
        val glide = GlideApp.with(this)
        GlobalScope.launch {
            with (video) {
                val thumbnail = glide.downloadOnly().load(preview.medium).submit().get().absolutePath
                val logo = glide.downloadOnly().load(channel.logo).submit().get().absolutePath
                repository.insert(OfflineVideo(playlistPath, title, channel.name, game, totalDuration, currentDate, createdAt, thumbnail, logo))
                reset()
            }
        }
        notificationBuilder.apply {
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            mActions.clear()
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        notificationManager.cancel(notificationId)
    }

    private fun reset() {
        requestToIdsMap.clear()
        tracks.clear()
        currentProgress = 0
        processedCount = 0
        totalDuration = 0L
        countDownLatch.countDown()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    class CancelReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            canceled = true
            val downloadManager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val iterator = requestToIdsMap.keyIterator()
            while (iterator.hasNext()) {
                downloadManager.remove(iterator.next())
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
        val url: String,
        val segments: ArrayList<Pair<String, Long>>,
        val targetDuration: Int) : Request() {

    override val id = video.id.substring(1).toInt()
    val maxProgress = segments.size
    var currentProgress = 0
    var totalDuration = 0L
    lateinit var directoryUri: Uri
    lateinit var directoryPath: String
}

data class ClipRequest(
        val clip: Clip,
        val quality: String,
        val url: String) : Request() {
    override val id = clip.slug.hashCode()
}
