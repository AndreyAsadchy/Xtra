package com.github.exact7.xtra.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.exact7.xtra.BuildConfig
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.Downloadable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.ui.download.DownloadService
import com.github.exact7.xtra.ui.download.KEY_REQUEST
import com.github.exact7.xtra.ui.download.KEY_TYPE
import com.google.gson.Gson
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import java.util.*

object DownloadUtils {

    private var fetch: Fetch? = null

    fun getFetch(context: Context): Fetch { //TODO dagger
        if (fetch == null || fetch!!.isClosed) {
            fetch = Fetch.getInstance(FetchConfiguration.Builder(context)
                    .enableLogging(BuildConfig.DEBUG)
                    .enableRetryOnNetworkGain(true)
                    .setDownloadConcurrentLimit(3)
                    .build())
        }
        return fetch!!
    }

    fun download(context: Context, request: Request) {
        val intent = Intent(context, DownloadService::class.java)
                .putExtra(KEY_REQUEST, Gson().toJson(request))
                .putExtra(KEY_TYPE, request is VideoRequest)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun prepareDownload(context: Context, downloadable: Downloadable, path: String, duration: Long): OfflineVideo {
        val offlinePath = if (downloadable is Video) {
            "$path${System.currentTimeMillis()}.m3u8"
        } else {
            "$path${downloadable.id}.mp4"
        }
        val glide = GlideApp.with(context)
        return with(downloadable) {
            val thumbnail = glide.downloadOnly().load(thumbnail).submit().get().absolutePath
            val logo = glide.downloadOnly().load(channelLogo).submit().get().absolutePath
            OfflineVideo(offlinePath, title, channelName, logo, thumbnail, game, duration, TwitchApiHelper.parseIso8601Date(uploadDate), Calendar.getInstance().time.time)
        }
    }

    fun hasStoragePermission(activity: Activity): Boolean {
        fun requestPermissions() {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return true
        }
        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            AlertDialog.Builder(activity)
                    .setMessage(R.string.storage_permission_message)
                    .setTitle(R.string.storage_permission_title)
                    .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions() }
//                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
        } else {
            // No explanation needed, we can request the permission.
            requestPermissions()
        }
        return false
    }
}
