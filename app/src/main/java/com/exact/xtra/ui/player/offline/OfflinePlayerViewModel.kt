package com.exact.xtra.ui.player.offline

import android.app.Application
import android.net.Uri

import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.ui.player.PlayerType
import com.exact.xtra.ui.player.PlayerViewModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource

import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application) : PlayerViewModel(context, PlayerType.VIDEO) {

    lateinit var video: OfflineVideo
    private var playbackProgress: Long = 0

    fun init() {
        val mediaSourceFactory = if (video.vod) {
            HlsMediaSource.Factory(dataSourceFactory)
        } else {
            ExtractorMediaSource.Factory(dataSourceFactory)
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
