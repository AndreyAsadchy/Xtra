package com.exact.twitch.ui.player.offline

import android.app.Application
import android.net.Uri

import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.repository.PlayerRepository
import com.exact.twitch.ui.player.PlayerType
import com.exact.twitch.ui.player.PlayerViewModel
import com.exact.twitch.util.DownloadUtils
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory

import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application,
        playerRepository: PlayerRepository) : PlayerViewModel(context, PlayerType.VIDEO) {

    lateinit var video: OfflineVideo
    private val factory = CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory)

    fun play() {
        val mediaSourceFactory = if (video.url.endsWith(".m3u8")) {
            HlsMediaSource.Factory(factory)
        } else {
            ExtractorMediaSource.Factory(factory)
        }
        play(mediaSourceFactory.createMediaSource(Uri.parse(video.url)))
    }
}
