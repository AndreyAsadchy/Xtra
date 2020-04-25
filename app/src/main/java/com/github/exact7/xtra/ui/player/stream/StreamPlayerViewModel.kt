package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.player.lowlatency.DefaultHlsPlaylistParserFactory
import com.github.exact7.xtra.player.lowlatency.DefaultHlsPlaylistTracker
import com.github.exact7.xtra.player.lowlatency.HlsManifest
import com.github.exact7.xtra.player.lowlatency.HlsMediaSource
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.AudioPlayerService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.exact7.xtra.ui.player.PlayerMode.DISABLED
import com.github.exact7.xtra.ui.player.PlayerMode.NORMAL
import com.github.exact7.xtra.util.toast
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    override val channelInfo: Pair<String, String>
        get() {
            val s = _stream.value!!
            return s.channel.id to s.channel.displayName
        }

    fun startStream(stream: Stream) {
        if (_stream.value == null) {
            _stream.value = stream
            viewModelScope.launch {
                try {
                    val uri = playerRepository.loadStreamPlaylist(stream.channel.name)
                    mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                            .setAllowChunklessPreparation(true)
                            .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
                            .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
                            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                            .createMediaSource(uri)
                    play()
                    launch {
                        while (isActive) {
                            val s = repository.loadStream(stream.channel.id).stream ?: break
                            _stream.postValue(s)
                            delay(300000L)
                        }
                    }
                } catch (e: Exception) {
                    val context = getApplication<Application>()
                    context.toast(R.string.error_stream)
                }
            }
        }
    }

    override fun changeQuality(index: Int) {
        previousQuality = qualityIndex
        super.changeQuality(index)
        when {
            index < qualities.size - 2 -> setVideoQuality(index)
            index < qualities.size - 1 -> {
                (player.currentManifest as? HlsManifest)?.let {
                    val s = _stream.value!!
                    startBackgroundAudio(helper.urls.values.last(), s.channel.status, s.channel.displayName, s.channel.logo, false, AudioPlayerService.TYPE_STREAM, null)
                    _playerMode.value = AUDIO_ONLY
                }
            }
            else -> {
                if (playerMode.value == NORMAL) {
                    player.stop()
                } else {
                    stopBackgroundAudio()
                }
                _playerMode.value = DISABLED
            }
        }
    }

    fun restartPlayer() {
        if (playerMode.value == NORMAL) {
            player.stop()
            play()
        } else if (playerMode.value == AUDIO_ONLY) {
            binder?.restartPlayer()
        }
    }
}
