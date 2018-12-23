package com.github.exact7.xtra.ui.player.stream

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.tasks.LiveChatTask
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.util.TwitchApiHelper
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val repository: PlayerRepository) : HlsPlayerViewModel(context) {

    val chatTask = MutableLiveData<LiveChatTask>()
    private var subscriberBadges: SubscriberBadgesResponse? = null
    private var _stream: Stream? = null
    var stream: Stream
        get() = _stream!!
        set(value) {
            if (_stream != value) {
                _stream = value
                init()
            }
        }
    var user: User? = null
        set(value) {
            if (field != value) {
                field = value
//                if (isInitialized()) {
//                    if (chatTask.value != null)
//                        stopChat()
//                    startChat()
//                }
            }
        }

    private fun init() {
        val channel = stream.channel
        repository.fetchStreamPlaylist(channel.name)
                .subscribe({
//                    mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
//                    play()
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

    fun startChat() {
        chatTask.postValue(TwitchApiHelper.startChat(stream.channel.name, user?.name, user?.token, subscriberBadges, helper))
    }

    fun stopChat() {
        chatTask.value?.cancel()
    }

    override fun onCleared() {
        stopChat()
        super.onCleared()
    }

}
