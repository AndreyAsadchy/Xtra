package com.github.exact7.xtra.ui.pagers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ChannelPagerViewModel @Inject constructor(
        private val repository: TwitchService) : ViewModel(), FollowViewModel {

    private val compositeDisposable = CompositeDisposable()
    private val _channel = MutableLiveData<Channel>()
    val channel: LiveData<Channel>
        get() = _channel
    private val _stream = Transformations.switchMap(_channel) {
        repository.loadStream(it.id, compositeDisposable)
    }
    val stream: LiveData<StreamWrapper>
        get() = _stream

    override val channelInfo: Pair<String, String>
        get() {
            val c = _channel.value!!
            return c.id to c.name
        }

    override lateinit var follow: FollowLiveData

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first)
        }
    }

    fun loadStream(channel: Channel) {
        if (this._channel.value != channel) {
            this._channel.value = channel
        }
    }
}
