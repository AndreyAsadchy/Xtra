package com.github.andreyasadchy.xtra.ui.player.offline

import android.app.Application
import androidx.core.net.toUri
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.ui.player.AudioPlayerService
import com.github.andreyasadchy.xtra.ui.player.PlayerMode
import com.github.andreyasadchy.xtra.ui.player.PlayerViewModel
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application,
        private val repository: OfflineRepository) : PlayerViewModel(context) {

    private lateinit var video: OfflineVideo
    val qualities = listOf(context.getString(R.string.video), context.getString(R.string.audio_only))

    fun setVideo(video: OfflineVideo) {
        if (!this::video.isInitialized) {
            this.video = video
            val mediaSourceFactory = if (video.vod) {
                HlsMediaSource.Factory(dataSourceFactory)
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
            }
            mediaSource = mediaSourceFactory.createMediaSource(video.url.toUri())
            play()
            player.seekTo(video.lastWatchPosition)
        }
    }

    override fun onResume() {
        isResumed = true
        if (playerMode.value == PlayerMode.NORMAL) {
            super.onResume()
            player.seekTo(playbackPosition)
        } else {
            hideBackgroundAudio()
        }
    }

    override fun onPause() {
        isResumed = false
        if (playerMode.value == PlayerMode.NORMAL) {
            playbackPosition = player.currentPosition
            super.onPause()
        } else {
            showBackgroundAudio()
        }
    }

    override fun changeQuality(index: Int) {
        qualityIndex = index
        _playerMode.value = if (qualityIndex == 0) {
            playbackPosition = currentPlayer.value!!.currentPosition
            stopBackgroundAudio()
            _currentPlayer.value = player
            play()
            player.seekTo(playbackPosition)
            PlayerMode.NORMAL
        } else {
            startBackgroundAudio(video.url, video.channelName, video.name, video.channelLogo, true, AudioPlayerService.TYPE_OFFLINE, video.id)
            PlayerMode.AUDIO_ONLY
        }
    }

    override fun onCleared() {
        if (playerMode.value == PlayerMode.NORMAL) {
            repository.updateVideoPosition(video.id, player.currentPosition)
        } else if (isResumed) {
            stopBackgroundAudio()
        }
        super.onCleared()
    }
}
