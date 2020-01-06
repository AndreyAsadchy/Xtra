package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import android.widget.Toast
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.exact7.xtra.ui.player.PlayerMode.DISABLED
import com.github.exact7.xtra.ui.player.PlayerMode.NORMAL
import com.github.exact7.xtra.ui.player.lowlatency.DefaultHlsPlaylistParserFactory
import com.github.exact7.xtra.ui.player.lowlatency.DefaultHlsPlaylistTracker
import com.github.exact7.xtra.ui.player.lowlatency.HlsMediaSource
import com.google.android.exoplayer2.source.hls.HlsManifest
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository) {

    private lateinit var stream: Stream
    override val channelInfo: Pair<String, String>
        get() = stream.channel.id to stream.channel.displayName

    fun startStream(stream: Stream) {
        if (!this::stream.isInitialized) {
            this.stream = stream
            playerRepository.loadStreamPlaylist(stream.channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory)
//                                .setAllowChunklessPreparation(true)
                                .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
                                .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
//                                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                                .createMediaSource(it)
                        play()

                    }, {
                        val context = getApplication<Application>()
                        Toast.makeText(context, context.getString(R.string.error_stream), Toast.LENGTH_LONG).show()
                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun changeQuality(index: Int) {
        super.changeQuality(index)
        when {
            index < qualities.size - 2 -> setVideoQuality(index)
            index < qualities.size - 1 -> {
                (player.currentManifest as? HlsManifest)?.let {
                    startBackgroundAudio(it.masterPlaylist.baseUri, stream.channel.status, stream.channel.displayName, stream.channel.logo, false)
                    _playerMode.value = AUDIO_ONLY
                }
            }
            else -> {
                player.stop()
                if (playerMode.value == AUDIO_ONLY) {
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
