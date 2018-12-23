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
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.util.contains
import androidx.core.util.keyIterator
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.OfflineVideo
import com.github.exact7.xtra.model.kraken.video.Video
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
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class VideoDownloadService : Service() {

    companion object {
        private const val VIDEO = "video"
        private const val QUALITY = "quality"
        private const val URL = "url"
        private const val SEGMENTS = "segments"
        private const val TARGET = "target"

        private const val TAG = "VideoDownloadService"
        private const val CHANNEL_ID = "xtra_download_channel"
        private val idsMap = LongSparseArray<Int>()
        private var canceled = false
        private val downloadQueue: Queue<Bundle> = LinkedList()

        fun addToQueue(video: Video, quality: String, url: String, segments: ArrayList<Pair<String, Long>>, targetDuration: Int) {
            downloadQueue.add(bundleOf(VIDEO to video, QUALITY to quality, URL to url, SEGMENTS to segments, TARGET to targetDuration))
        }
    }

    @Inject
    lateinit var dao: VideosDao //TODO change to repository

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private val notificationId = System.currentTimeMillis().toInt() //TODO change to video id and handle notification clicks
    private lateinit var cancelAction: NotificationCompat.Action
    private var countDownLatch = CountDownLatch(0)

    private lateinit var video: Video
    private lateinit var quality: String
    private lateinit var url: String
    private var targetDuration = 0
    private lateinit var segments: List<Pair<String, Long>>

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
    private var maxProgress = 0
    private var currentProgress = 0
    private var processedCount = 0
    private var totalDuration = 0L
    private lateinit var directoryPath: String
    private lateinit var directoryUri: Uri
    private val receiver = object : BroadcastReceiver() {

        fun process(intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val segmentIndex = idsMap.get(id)
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
                if (!idsMap.contains(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1))) return //TODO maybe do one download service?
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
                        notificationBuilder.mActions.remove(cancelAction)
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
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            priority = NotificationCompat.PRIORITY_LOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Xtra", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val cancelIntent = PendingIntent.getBroadcast(this, 0, Intent(this, CancelReceiver::class.java), 0)
        cancelAction = NotificationCompat.Action(0, getString(R.string.cancel), cancelIntent)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Adding download to queue")
        GlobalScope.launch {
            countDownLatch.await()
            if (init(downloadQueue.poll())) {
                startDownload()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "unchecked_cast")
    private fun init(bundle: Bundle?): Boolean {
        if (bundle == null) return false
        with (bundle) {
            video = getParcelable(VIDEO) as Video
            quality = getString(QUALITY)
            url = getString(URL)
            targetDuration = getInt(TARGET)
            segments = getSerializable(SEGMENTS) as ArrayList<Pair<String, Long>>
        }
        with(getExternalFilesDir(".downloads" + File.separator + video.id + quality)) {
            directoryUri = toUri()
            directoryPath = absolutePath
        }
        maxProgress = segments.size
        notificationBuilder.apply {
            setContentTitle(getString(R.string.downloading))
            setContentText(video.title)
            setOngoing(true)
            addAction(cancelAction)
        }
        countDownLatch = CountDownLatch(1)
        return true
    }

    private fun startDownload() {
        Log.d(TAG, "Starting download")
        notificationBuilder.setProgress(maxProgress, currentProgress, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        enqueueNext()
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
                dao.insert(OfflineVideo(playlistPath, title, channel.name, game, totalDuration, currentDate, createdAt, thumbnail, logo))
                reset()
            }
        }
        notificationBuilder.apply {
            setAutoCancel(true)
            setContentTitle(getString(R.string.downloaded))
            setProgress(0, 0, false)
            setOngoing(false)
            mActions.remove(cancelAction)
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        notificationManager.cancel(notificationId)
    }

    private fun enqueueNext() {
        val pair = segments[currentProgress]
        val request = DownloadManager.Request((url + pair.first).toUri()).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            setVisibleInDownloadsUi(false)
            setDestinationUri(Uri.withAppendedPath(directoryUri, pair.first))
        }
        idsMap.put(downloadManager.enqueue(request), currentProgress)
    }

    private fun reset() {
        idsMap.clear()
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

    class CancelReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                canceled = true
                val downloadManager = it.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val iterator = idsMap.keyIterator()
                while (iterator.hasNext()) {
                    downloadManager.remove(iterator.next())
                }
            }
        }
    }
}
