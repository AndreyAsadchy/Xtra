package com.github.andreyasadchy.xtra.ui.channel.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.kraken.Channel
import com.github.andreyasadchy.xtra.model.kraken.stream.StreamWrapper
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelInfoViewModel @Inject constructor(private val repository: TwitchService) : ViewModel(), FollowViewModel {

    private val _channel = MutableLiveData<Channel>()
    val channel: LiveData<Channel>
        get() = _channel
    private val _stream = MutableLiveData<StreamWrapper>()
    val stream: LiveData<StreamWrapper>
        get() = _stream

    override val channelInfo: Pair<String, String>
        get() {
            val c = _channel.value!!
            return c.id to c.displayName
        }

    override lateinit var follow: FollowLiveData

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first, viewModelScope)
        }
    }

    fun loadStream(channel: Channel) {
        if (_channel.value != channel) {
            _channel.value = channel
            viewModelScope.launch {
                try {
                    val stream = repository.loadStream(channel.id)
                    _stream.postValue(stream)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun retry() {
        if (_stream.value == null) {
            _channel.value?.let {
                loadStream(it)
            }
        }
    }
}
