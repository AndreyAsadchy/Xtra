package com.exact.xtra.service

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.util.keyIterator
import com.exact.xtra.GlideApp
import com.exact.xtra.R
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.model.video.Video
import com.exact.xtra.util.TwitchApiHelper
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
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class VideoDownloadService : Service() {

    private companion object {
        const val TAG = "VideoDownloadService"
        const val CHANNEL_ID = "download_channel"
        val idsMap = LongSparseArray<Int>()
        var canceled = false
    }

    @Inject
    lateinit var dao: VideosDao
    private val downloadQueue: Queue<Bundle> = LinkedList()

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
    private lateinit var directoryUri: Uri
    private lateinit var directoryPath: String


    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            priority = NotificationCompat.PRIORITY_HIGH
        }
        val cancelIntent = PendingIntent.getBroadcast(this, 0, Intent(this, CancelReceiver::class.java), 0)
        cancelAction = NotificationCompat.Action(0, getString(R.string.cancel), cancelIntent)
        val receiver = object : BroadcastReceiver() {

            private var stopped = false

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
                        if (!stopped) {
                            stopped = true
                            Log.d(TAG, "Canceled download")
                            notificationManager.cancel(notificationId)
                            reset()
                            if (processedCount == maxProgress) {
                                stopped = false
                                canceled = false
                            }
                        }
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadRequest = intent!!.extras
        downloadQueue.add(downloadRequest)
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
            video = getParcelable("video") as Video
            quality = getString("quality")
            url = getString("url")
            targetDuration = getInt("target")
            segments = getSerializable("segments") as ArrayList<Pair<String, Long>>
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

    class CancelReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                canceled = true
                val downloadManager = it.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val array = LongArray(idsMap.size()).apply {
                    var i = 0
                    val iterator = idsMap.keyIterator()
                    while (iterator.hasNext()) {
                        this[i++] = iterator.next()
                    }
                }
                downloadManager.remove(*array)
            }
        }
    }
}
