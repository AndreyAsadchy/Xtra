package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository, playerRepository) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    lateinit var user: User
        private set
    override val channelInfo: Pair<String, String>
        get() {
            val s = stream.value!!
            return s.channel.id to s.channel.displayName
        }

    fun startStream(stream: Stream, user: User) {
        if (_stream.value != stream) {
            _stream.value = stream
            this.user = user
            val channel = stream.channel
            playerRepository!!.fetchStreamPlaylist(channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {
                        val context = getApplication<Application>()
                        Toast.makeText(context, context.getString(R.string.error_stream), Toast.LENGTH_LONG).show()
                    })
                    .addTo(compositeDisposable)
            init(channel.id, channel.name, streamChatCallback = this::startChat)
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

    override fun onResume() {
        super.onResume()
        startChat()
    }

    override fun onPause() {
        super.onPause()
        stopChat()
    }

    private fun startChat() {
        val userName = user.name.let {
            if (it == "") {
                null
            } else {
                it
            }
        }

        val userToken = user.token.let {
            if (it == "") {
                null
            } else {
                it
            }
        }
        _chat.value = TwitchApiHelper.startChat(stream.value!!.channel.name, userName, userToken, subscriberBadges, this)
    }

    private fun stopChat() {
        _chat.value?.disconnect()
    }

    override fun onCleared() {
        stopChat()
        super.onCleared()
    }
}
