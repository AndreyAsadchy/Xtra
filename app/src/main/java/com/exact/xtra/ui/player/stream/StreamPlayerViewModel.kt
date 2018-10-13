package com.exact.xtra.ui.player.stream

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.exact.xtra.model.User
import com.exact.xtra.model.chat.SubscriberBadgesResponse
import com.exact.xtra.model.stream.Stream
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.tasks.LiveChatTask
import com.exact.xtra.ui.player.HlsPlayerViewModel
import com.exact.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val repository: PlayerRepository) : HlsPlayerViewModel(context) {

    val chatTask = MutableLiveData<LiveChatTask>()
    private lateinit var subscriberBadges: SubscriberBadgesResponse
    private var _stream: Stream? = null
    var stream: Stream
        get() = _stream!!
        set(value) {
            if (_stream != value) {
                _stream = value
                play()
            }
        }
    var user: User? = null
        set(value) {
            field = value
            if (this::subscriberBadges.isInitialized) {
                startChat()
            }
        }

    override fun play() {
        if (isInitialized()) {
            super.play()
            return
        }
        val channel = stream.channel
        repository.fetchStreamPlaylist(channel.name)
                .subscribe({
                    mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                    super.play()
                }, {

                })
                .addTo(compositeDisposable)
        repository.fetchSubscriberBadges(stream.id.toInt())
                .subscribe({
                    subscriberBadges = it
                    startChat()
                }, {

                })
                .addTo(compositeDisposable)
    }

    fun startChat() {
        if (this::subscriberBadges.isInitialized)
        chatTask.postValue(TwitchApiHelper.startChat(stream.channel.name, user?.name, user?.token, subscriberBadges, helper))
    }

    override fun onCleared() {
        chatTask.value?.shutdown()
        super.onCleared()
    }
}
