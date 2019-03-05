package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val repository: PlayerRepository) : HlsPlayerViewModel(context) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    private val _chat = MutableLiveData<LiveChatThread>()
    val chat: LiveData<LiveChatThread>
        get() = _chat
    private val _bttv = MutableLiveData<List<BttvEmote>>()
    val bttv: LiveData<List<BttvEmote>>
        get() = _bttv
    private val _ffz = MutableLiveData<List<FfzEmote>>()
    val ffz: LiveData<List<FfzEmote>>
        get() = _ffz
    private var subscriberBadges: SubscriberBadgesResponse? = null
    lateinit var user: User
        private set

    fun startStream(stream: Stream, user: User) {
        if (_stream.value != stream) {
            _stream.value = stream
            this.user = user
            val channel = stream.channel
            repository.fetchStreamPlaylist(channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {
                        val context = getApplication<Application>()
                        Toast.makeText(context, context.getString(R.string.error_stream), Toast.LENGTH_LONG).show()
                    })
                    .addTo(compositeDisposable)
            repository.fetchSubscriberBadges(channel.id)
                    .subscribe({
                        subscriberBadges = it
                        startChat()
                    }, { //no subscriber badges
                        startChat()
                    })
                    .addTo(compositeDisposable)
            repository.fetchBttvEmotes(channel.name)
                    .subscribe({
                        _bttv.value = it
                    }, {

                    })
                    .addTo(compositeDisposable)
            repository.fetchFfzEmotes(channel.name)
                    .subscribe({
                        _ffz.value = it
                    }, {

                    })
                    .addTo(compositeDisposable)
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
        _chat.value = TwitchApiHelper.startChat(stream.value!!.channel.name, userName, userToken, subscriberBadges, helper)
    }

    private fun stopChat() {
        _chat.value?.cancel()
    }

    override fun onCleared() {
        stopChat()
        super.onCleared()
    }
}
