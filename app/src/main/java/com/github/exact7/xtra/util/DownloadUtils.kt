package com.github.exact7.xtra.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.Downloadable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.ui.download.BaseDownloadDialog.Storage
import com.github.exact7.xtra.ui.download.DownloadService
import com.github.exact7.xtra.ui.download.KEY_REQUEST
import com.github.exact7.xtra.ui.download.KEY_TYPE
import com.github.exact7.xtra.ui.download.KEY_WIFI
import com.google.gson.Gson
import java.io.File
import java.util.Calendar

object DownloadUtils {

    val isExternalStorageAvailable: Boolean
        get() {
            var state: String? = null
            return try {
                state = Environment.getExternalStorageState()
                state == Environment.MEDIA_MOUNTED
            } catch (e: Exception) {
                Crashlytics.log("DownloadUtils.isExternalStorageAvailable: ${e.message}. State: $state ")
                false
            }
        }

    fun download(context: Context, request: Request, wifiOnly: Boolean = false) {
        val intent = Intent(context, DownloadService::class.java)
                .putExtra(KEY_REQUEST, Gson().toJson(request))
                .putExtra(KEY_TYPE, request is VideoRequest)
                .putExtra(KEY_WIFI, wifiOnly)
        context.startService(intent)
    }

//    fun createClipRequest()

    fun prepareDownload(context: Context, downloadable: Downloadable, url: String, path: String, duration: Long, startPosition: Long?): OfflineVideo {
        val offlinePath = if (downloadable is Video) {
            "$path${System.currentTimeMillis()}.m3u8"
        } else {
            "$path.mp4"
        }
        val glide = GlideApp.with(context)
        return with(downloadable) {
            val thumbnail = glide.downloadOnly().load(thumbnail).submit().get().absolutePath
            val logo = glide.downloadOnly().load(channelLogo).submit().get().absolutePath
            OfflineVideo(offlinePath, url, startPosition, title, channelName, logo, thumbnail, game, duration, TwitchApiHelper.parseIso8601Date(uploadDate), Calendar.getInstance().time.time)
        }
    }

    fun hasInternalStoragePermission(activity: Activity): Boolean {
        fun requestPermissions() {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(activity)
                    .setMessage(R.string.storage_permission_message)
                    .setTitle(R.string.storage_permission_title)
                    .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> Toast.makeText(activity, activity.getString(R.string.permission_denied), Toast.LENGTH_LONG).show() }
                    .show()
        } else {
            requestPermissions()
        }
        return false
    }

    fun getAvailableStorage(context: Context): List<Storage> {
        val storage = ContextCompat.getExternalFilesDirs(context, ".downloads")
        val list = mutableListOf<Storage>()
        for (i in 0 until storage.size) {
            val storagePath = storage[i].absolutePath
            val name = if (i == 0) {
                context.getString(R.string.internal_storage)
            } else {
                val endRootIndex = storagePath.indexOf("/Android/data")
                if (endRootIndex < 0) continue
                var startRootIndex = 0
                for (j in 1 until endRootIndex) {
                    if (storagePath[j].toString() == File.separator) {
                        startRootIndex = j
                    }
                }
                storagePath.substring(startRootIndex + 1, endRootIndex)
            }
            list.add(Storage(i, name, storagePath))
        }
        return list
    }
}
