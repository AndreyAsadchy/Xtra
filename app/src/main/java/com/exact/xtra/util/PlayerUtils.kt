package com.exact.xtra.util

import android.content.Context
import android.content.Intent

import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.service.MediaDownloadService
import com.google.android.exoplayer2.offline.DownloadAction

import com.google.android.exoplayer2.offline.DownloadService.ACTION_ADD
import com.google.android.exoplayer2.offline.DownloadService.KEY_DOWNLOAD_ACTION
import com.google.android.exoplayer2.offline.DownloadService.KEY_FOREGROUND

object PlayerUtils {

    fun startDownload(context: Context, downloadAction: DownloadAction, video: OfflineVideo) {
        val intent = Intent(context, MediaDownloadService::class.java)
                .setAction(ACTION_ADD)
                .putExtra(KEY_DOWNLOAD_ACTION, downloadAction.toByteArray())
                .putExtra(KEY_FOREGROUND, false)
                .putExtra("video", video)
        context.startService(intent)
    }
}
