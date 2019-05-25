package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
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
            val s = stream.value!!
            return s.channel.id to s.channel.displayName
        }

    override val loadControl: DefaultLoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS, 1500, 3000)
            .createDefaultLoadControl()

    fun startStream(stream: Stream) {
        if (_stream.value != stream) {
            _stream.value = stream
            val channel = stream.channel
            call(playerRepository.loadStreamPlaylist(channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                                .setAllowChunklessPreparation(true)
                                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                                .createMediaSource(it)
                        play()
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
}
