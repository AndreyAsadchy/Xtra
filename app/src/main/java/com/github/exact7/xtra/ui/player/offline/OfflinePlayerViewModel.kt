package com.github.exact7.xtra.ui.player.offline

import android.app.Application
import androidx.core.net.toUri
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application,
        private val repository: OfflineRepository) : PlayerViewModel(context) {

    private lateinit var video: OfflineVideo

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
        stopBackgroundAudio()
    }

    override fun onPause() {
        startBackgroundAudio(video.url, video.channelName, video.name, true)
    }

    override fun onCleared() {
        launch {
            repository.updateVideo(video.apply {
                lastWatchPosition = runBlocking(Dispatchers.Main) { player.currentPosition }
            })
        }
        super.onCleared()
    }
}
