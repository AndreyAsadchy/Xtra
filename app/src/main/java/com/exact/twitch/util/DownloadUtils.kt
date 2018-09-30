package com.exact.twitch.util

import android.content.Context
import com.exact.twitch.R
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadAction
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

object DownloadUtils {

    @Volatile private var cache: Cache? = null
    @Volatile private var downloadManager: DownloadManager? = null

    fun getCache(context: Context): Cache =
            cache ?: synchronized(this) {
                cache ?: SimpleCache(File(context.getExternalFilesDir(null), "downloads"), NoOpCacheEvictor())
                        .also { cache = it }
            }

    fun getDownloadManager(context: Context): DownloadManager =
            downloadManager ?: synchronized(this) {
                downloadManager ?: DownloadManager(
                        getCache(context),
                        DefaultDataSourceFactory(
                                context,
                                Util.getUserAgent(context, context.getString(R.string.app_name))),
                        File(context.externalCacheDir, "actions"),
                        HlsDownloadAction.DESERIALIZER, ProgressiveDownloadAction.DESERIALIZER)
                        .also { downloadManager = it }
            }
}
