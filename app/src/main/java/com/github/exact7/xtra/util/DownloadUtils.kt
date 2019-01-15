package com.github.exact7.xtra.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.exact7.xtra.BuildConfig
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.Downloadable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.service.DownloadService
import com.github.exact7.xtra.service.KEY_REQUEST
import com.github.exact7.xtra.service.KEY_TYPE
import com.google.gson.Gson
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration

object DownloadUtils {

    private var fetch: Fetch? = null

    fun getFetch(context: Context): Fetch {
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
            "$path.mp4"
        }
        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
        val glide = GlideApp.with(context)
        return with(downloadable) {
            val thumbnail = glide.downloadOnly().load(thumbnail).submit().get().absolutePath
            val logo = glide.downloadOnly().load(channelLogo).submit().get().absolutePath
            OfflineVideo(offlinePath, title, channelName, logo, thumbnail, game, duration, uploadDate, currentDate)
        }
    }
}