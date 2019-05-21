package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    override val channelInfo: Pair<String, String>
        get() {
            val s = stream.value!!
            return s.channel.id to s.channel.displayName
        }

    private var seekTimer: Timer? = null
    private var timeSpentBuffering = 0L
    private var bufferedAt = 0L

    fun startStream(stream: Stream) {
        if (_stream.value != stream) {
            _stream.value = stream
            val channel = stream.channel
            call(playerRepository.loadStreamPlaylist(channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                        seekTimer = fixedRateTimer(period = 10000L, action = {
                            runBlocking(Dispatchers.Main) {
                                if (timeSpentBuffering > 5000L && player.playWhenReady) {
                                    try {
                                        player.seekToDefaultPosition(player.currentTimeline.getLastWindowIndex(false))
                                        timeSpentBuffering = 0L
                                    } catch (e: Exception) {
                                        Crashlytics.logException(e)
                                    }
                                }
                            }
                        })
                    }, {
                        val context = getApplication<Application>()
                        Toast.makeText(context, context.getString(R.string.error_stream), Toast.LENGTH_LONG).show()
                    }))
        }
    }

    override fun changeQuality(index: Int) {
        super.changeQuality(index)
        when {
            index < qualities.size - 2 -> updateQuality(index)
            index < qualities.size - 1 -> changePlayerMode(PlayerMode.AUDIO_ONLY)
            else -> changePlayerMode(PlayerMode.DISABLED)
        }
    }

    override fun onCleared() {
        seekTimer?.cancel()
        super.onCleared()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> bufferedAt = System.currentTimeMillis()
            Player.STATE_READY -> timeSpentBuffering += System.currentTimeMillis() - bufferedAt
        }
    }
}
