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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
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
import javax.inject.Inject

class VideoDownloadService : Service() {

    private companion object {
        const val TAG = "VideoDownloadService"
        const val CHANNEL_ID = "download_channel"
        val idsMap = HashMap<Long, Int>()
        var canceled = false
    }
    @Inject
    lateinit var dao: VideosDao
    private var isDownloading = false
    private val downloadQueue: Queue<Pair<Bundle, Int>> = LinkedList()

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var cancelAction: NotificationCompat.Action

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
    private var totalDuration = 0L
    private lateinit var directoryUri: Uri
    private lateinit var directoryPath: String

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "unchecked_cast")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadRequest = intent!!.extras to System.currentTimeMillis().toInt() //TODO implement download queue
        downloadQueue.add(downloadRequest)
        if (!isDownloading)
        with (intent.extras) {
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
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cancelIntent = PendingIntent.getBroadcast(this, 0, Intent(this, CancelReceiver::class.java).putExtra("id", notificationId), Intent.FILL_IN_DATA)
        cancelAction = NotificationCompat.Action(0, getString(R.string.cancel), cancelIntent)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getString(R.string.downloading))
            setContentText(video.title)
            setOngoing(true)
            setSmallIcon(R.drawable.ic_notification)
            priority = NotificationCompat.PRIORITY_HIGH
            addAction(cancelAction)
        }
        notificationManager = NotificationManagerCompat.from(this)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.run {
                    if (!canceled) {
                        val id = getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        val segmentIndex = idsMap[id]!!
                        val segment = segments[segmentIndex]
                        val trackDuration = segment.second
                        totalDuration += trackDuration
                        tracks.add(TrackData.Builder()
                                .withUri(directoryPath + File.separator + segment.first)
                                .withTrackInfo(TrackInfo(trackDuration.toFloat(), segment.first))
                                .build())
                        if (++currentProgress != maxProgress) {
                            notificationBuilder.setProgress(maxProgress, currentProgress, false)
                        } else {
                            onDownloadCompleted()
                        }
                        notificationManager.notify(notificationId, notificationBuilder.build())
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        startDownload()
        return super.onStartCommand(intent, flags, startId)

    }

    private fun startDownload() {
        Log.d(TAG, "Starting download")
        notificationBuilder.setProgress(maxProgress, currentProgress, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        segments.forEachIndexed { index, pair ->
            val request = DownloadManager.Request((url + pair.first).toUri()).apply {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setVisibleInDownloadsUi(false)
                setDestinationUri(Uri.withAppendedPath(directoryUri, pair.first))
            }
            idsMap[downloadManager.enqueue(request)] = index
        }
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
            }
        }
        notificationBuilder
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.downloaded))
                .setProgress(0, 0, false)
                .setOngoing(false)
                .mActions.remove(cancelAction)
    }

    class CancelReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                canceled = true
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.remove(*idsMap.keys.toLongArray())
                val notificationBuilder = NotificationCompat.Builder(it, CHANNEL_ID).apply {
                    setAutoCancel(true)
                    setContentTitle(it.getString(R.string.canceled))
                    setProgress(0, 0 , false)
                    setOngoing(false)
                    setSmallIcon(R.drawable.ic_notification)
                    priority = NotificationCompat.PRIORITY_HIGH
                }
                NotificationManagerCompat.from(it).notify(intent!!.getIntExtra("id", -1), notificationBuilder.build())
            }
        }
    }
}
