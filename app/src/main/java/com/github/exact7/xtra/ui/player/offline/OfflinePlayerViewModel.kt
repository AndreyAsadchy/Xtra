package com.github.exact7.xtra.ui.player.offline

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class OfflinePlayerViewModel @Inject constructor(
        context: Application,
        private val repository: OfflineRepository) : PlayerViewModel(context) {

    private val _video = MutableLiveData<OfflineVideo>()
    val video: LiveData<OfflineVideo>
        get() = _video
    private var playbackProgress: Long = 0

    fun setVideo(video: OfflineVideo) {
        if (_video.value != video) {
            _video.value = video
            val mediaSourceFactory = if (video.vod) {
                HlsMediaSource.Factory(dataSourceFactory)
            } else {
                ExtractorMediaSource.Factory(dataSourceFactory)
            }
            mediaSource = mediaSourceFactory.createMediaSource(video.url.toUri())
            play()
            player.seekTo(video.lastWatchPosition)
        }
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackProgress)
    }

    override fun onPause() {
        super.onPause()
        playbackProgress = player.currentPosition
    }

    override fun onCleared() {
        _video.value?.let {
            GlobalScope.launch {
                repository.updateVideo(it.apply {
                    lastWatchPosition = runBlocking(Dispatchers.Main) { player.currentPosition }
                })
            }
        }
        super.onCleared()
    }
}
