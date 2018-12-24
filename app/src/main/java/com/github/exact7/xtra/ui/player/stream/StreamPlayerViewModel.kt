package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.tasks.LiveChatTask
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val repository: PlayerRepository) : HlsPlayerViewModel(context) {

    private val _stream = MutableLiveData<Stream>()
    val stream: LiveData<Stream>
        get() = _stream
    val chatTask = MutableLiveData<LiveChatTask>()
    private var subscriberBadges: SubscriberBadgesResponse? = null
    var user: User? = null
        private set

    fun startStream(stream: Stream, user: User?) {
        if (_stream.value != stream) {
            _stream.value = stream
            this.user = user
            val channel = stream.channel
            repository.fetchStreamPlaylist(channel.name)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {

                    })
                    .addTo(compositeDisposable)
            repository.fetchSubscriberBadges(stream.channel.id)
                    .subscribe({
                        subscriberBadges = it
                        startChat()
                    }, { //no subscriber badges
                        startChat()
                    })
                    .addTo(compositeDisposable)
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
        chatTask.value = TwitchApiHelper.startChat(stream.value!!.channel.name, user?.name, user?.token, subscriberBadges, helper)
    }

    private fun stopChat() {
        chatTask.value?.cancel()
    }

    override fun onCleared() {
        stopChat()
        super.onCleared()
    }
}
