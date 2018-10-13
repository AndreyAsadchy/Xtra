package com.exact.xtra.ui.player.offline

import android.app.Application
import android.net.Uri

import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.ui.player.PlayerType
import com.exact.xtra.ui.player.PlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory

import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application,
        playerRepository: PlayerRepository) : PlayerViewModel(context, PlayerType.VIDEO) {

    lateinit var video: OfflineVideo
    private val factory = CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory)
    private var playbackProgress: Long = 0

    fun init() {
        val mediaSourceFactory = if (video.url.endsWith(".m3u8")) {
            HlsMediaSource.Factory(factory)
        } else {
            ExtractorMediaSource.Factory(factory)
        }
        mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(video.url))
        startPlayer()
    }

    override fun startPlayer() {
        super.startPlayer()
        player.seekTo(playbackProgress)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }
}
