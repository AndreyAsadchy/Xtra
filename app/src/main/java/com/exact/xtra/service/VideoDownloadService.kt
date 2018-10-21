package com.exact.xtra.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class VideoDownloadService : Service() {

    private companion object {
        const val TAG = "VideoDownloadService"
        const val CHANNEL_ID = "download_channel"
    }

    @Inject
    lateinit var dao: VideosDao
    private lateinit var downloadTask: Deferred<Unit>
    private lateinit var notificationBuilder: NotificationCompat.Builder 
    private lateinit var notificationManager: NotificationManagerCompat
    private val notificationId = System.currentTimeMillis().toInt()

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "unchecked_cast")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with (intent!!.extras) {
            val video = getParcelable("video") as Video
            notificationBuilder = NotificationCompat.Builder(this@VideoDownloadService, CHANNEL_ID).apply {
                setContentTitle(getString(R.string.downloading))
                setContentText(video.title)
                setOngoing(true)
                setSmallIcon(R.drawable.ic_notification)
                priority = NotificationCompat.PRIORITY_HIGH
            }
            notificationManager = NotificationManagerCompat.from(this@VideoDownloadService)
            download(
                    video,
                    getString("quality"),
                    getString("url"),
                    getSerializable("segments") as ArrayList<Pair<String, Long>>,
                    getInt("target"))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun download(video: Video, quality: String, url: String, segments: ArrayList<Pair<String, Long>>, targetDuration: Int) {
        Log.d(TAG, "Starting download")
        val okHttpClient = OkHttpClient()
        val directory = getDir(video.id + quality, Context.MODE_PRIVATE)
        var totalDuration = 0L
        val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
            fun parse(trackData: TrackData) = trackData.uri.substring(trackData.uri.lastIndexOf('/') + 1, trackData.uri.lastIndexOf('.')).toInt()

            val index1 = parse(o1)
            val index2 = parse(o2)
            when {
                index1 > index2 -> 1
                index1 < index2 -> -1
                else -> 0
            }
        })
        val maxProgress = segments.size
        var currentProgress = 0
        notificationManager.apply {
            notificationBuilder.setProgress(maxProgress, currentProgress, false)
            notify(notificationId, notificationBuilder.build())
            downloadTask = async {
                segments.forEach {
                    val request = Request.Builder().url(url + it.first).build()
                    val response = okHttpClient.newCall(request).execute()
                    response.body()!!.byteStream()!!.run {
                        val file = File(directory, it.first)
                        val out = FileOutputStream(file)
                        copyTo(out)
                        close()
                        out.close()
                        val trackDuration = it.second
                        totalDuration += trackDuration
                        tracks.add(TrackData.Builder()
                                .withUri(file.absolutePath)
                                .withTrackInfo(TrackInfo(trackDuration.toFloat(), it.first))
                                .build())
                        notificationBuilder.setProgress(maxProgress, ++currentProgress, false)
                        notify(notificationId, notificationBuilder.build())
                    }
                }
            }
            launch {
                downloadTask.await()
                Log.d(TAG, "Downloading done. Creating playlist")
                val mediaPlaylist = MediaPlaylist.Builder()
                        .withTargetDuration(targetDuration)
                        .withTracks(tracks.toList())
                        .build()
                val playlist = Playlist.Builder()
                        .withMediaPlaylist(mediaPlaylist)
                        .build()
                val playlistPath = directory.absolutePath + "/${System.currentTimeMillis()}.m3u8"
                val out = FileOutputStream(playlistPath)
                val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
                writer.write(playlist)
                out.close()
                Log.d(TAG, "Playlist created. Saving video")
                val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this@VideoDownloadService)
                val glide = GlideApp.with(this@VideoDownloadService)
                val thumbnail = glide.downloadOnly().load(video.preview.medium).submit().get().absolutePath
                val logo = glide.downloadOnly().load(video.channel.logo).submit().get().absolutePath
                dao.insert(OfflineVideo(playlistPath, video.title, video.channel.name, video.game, totalDuration, currentDate, video.createdAt, thumbnail, logo))
                notificationBuilder
                        .setAutoCancel(true)
                        .setContentTitle(getString(R.string.downloaded))
                        .setProgress(0, 0 , false)
                        .setOngoing(false)
                notify(notificationId, notificationBuilder.build())
            }
        }
    }
}
