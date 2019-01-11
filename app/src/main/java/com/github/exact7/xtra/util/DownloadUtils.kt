package com.github.exact7.xtra.util

import android.content.Context
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.Downloadable
import com.github.exact7.xtra.model.offline.OfflineVideo
import java.io.File

object DownloadUtils {

    fun prepareDownload(context: Context, downloadable: Downloadable, path: String, duration: Long): OfflineVideo {
        val offlinePath = if (downloadable is Video) {
            "$path${File.separator}${System.currentTimeMillis()}.m3u8"
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