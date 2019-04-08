package com.github.exact7.xtra.ui.pagers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class ChannelPagerViewModel @Inject constructor(
        private val repository: TwitchService) : ViewModel(), FollowViewModel {

    private val compositeDisposable = CompositeDisposable()
    private val _channel = MutableLiveData<Channel>()
    val channel: LiveData<Channel>
        get() = _channel
    private val _stream = MutableLiveData<StreamWrapper>()
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
        if (_channel.value != channel) {
            _channel.value = channel
            repository.loadStream(channel.id)
                    .subscribeBy(onSuccess = {
                        _stream.value = it
                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun retry() {
        if (_stream.value == null) {
            _channel.value?.let {
                loadStream(it)
            }
        }
    }
}
