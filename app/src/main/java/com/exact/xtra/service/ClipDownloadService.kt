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
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.util.TwitchApiHelper
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ClipDownloadService : Service() {

    private companion object {
        const val TAG = "ClipDownloadService"
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
            val clip = getParcelable("clip") as Clip
            notificationBuilder = NotificationCompat.Builder(this@ClipDownloadService, CHANNEL_ID).apply {
                setContentTitle(getString(R.string.downloading))
                setContentText(clip.title)
                setOngoing(true)
                setSmallIcon(R.drawable.ic_notification)
                priority = NotificationCompat.PRIORITY_HIGH
            }
            notificationManager = NotificationManagerCompat.from(this@ClipDownloadService)
            download(
                    clip,
                    getString("quality"),
                    getString("url"))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun download(clip: Clip, quality: String, url: String) {
        Log.d(TAG, "Starting download")
        val okHttpClient = OkHttpClient()
        val directory = getDir(null, Context.MODE_PRIVATE)
        notificationBuilder.setProgress(0, 0, true)
        notificationManager.apply {
            notify(notificationId, notificationBuilder.build())
            downloadTask = async {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                response.body()!!.byteStream()!!.run {
                    val file = File(directory, clip.slug + quality)
                    val out = FileOutputStream(file)
                    copyTo(out)
                    close()
                    out.close()
                    Log.d(TAG, "Downloading done. Saving video")
                    val currentDate = TwitchApiHelper.getCurrentTimeFormatted(this@ClipDownloadService)
                    val glide = GlideApp.with(this@ClipDownloadService)
                    val thumbnail = glide.downloadOnly().load(clip.thumbnails.medium).submit().get().absolutePath
                    val logo = glide.downloadOnly().load(clip.broadcaster.logo).submit().get().absolutePath
                    with (clip) {
                        dao.insert(OfflineVideo(file.absolutePath, title, broadcaster.name, game, duration.toLong(), currentDate, createdAt, thumbnail, logo))
                    }
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
}
