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
import com.github.exact7.xtra.util.RemoteConfigParams
import com.github.exact7.xtra.util.toast
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
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
            loadStream(stream)
            viewModelScope.launch {
                while (isActive) {
                    try {
                        val s = repository.loadStream(stream.channel.id).stream ?: break
                        _stream.postValue(s)
                        delay(300000L)
                    } catch (e: Exception) {
                        delay(60000L)
                    }
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
                    startBackgroundAudio(helper.urls.values.last(), s.channel.displayName, s.channel.status, s.channel.logo, false, AudioPlayerService.TYPE_STREAM, null)
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

    override fun onResume() {
        isResumed = true
        if (playerMode.value == NORMAL) {
            loadStream(stream.value ?: return)
        } else if (playerMode.value == AUDIO_ONLY) {
            hideBackgroundAudio()
        }
    }

    override fun restartPlayer() {
        if (playerMode.value == NORMAL) {
            loadStream(stream.value ?: return)
        } else if (playerMode.value == AUDIO_ONLY) {
            binder?.restartPlayer()
        }
    }

    private fun loadStream(stream: Stream) {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener {
                    viewModelScope.launch {
                        try {
                            //for bypassing ads
                            val playerType = remoteConfig.getString(RemoteConfigParams.TWITCH_PLAYER_TYPE_KEY)

                            val uri = playerRepository.loadStreamPlaylist(stream.channel.name, playerType)
                            mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                                    .setAllowChunklessPreparation(true)
                                    .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
                                    .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
                                    .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                                    .createMediaSource(uri)
                            play()
                        } catch (e: Exception) {
                            val context = getApplication<Application>()
                            context.toast(R.string.error_stream)
                        }
                    }
                }
    }
}
