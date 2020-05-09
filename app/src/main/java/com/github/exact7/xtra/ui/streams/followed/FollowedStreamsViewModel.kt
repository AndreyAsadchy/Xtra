package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Pair<User, Boolean>>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        repository.loadFollowedStreams(it.first.token, StreamType.ALL, it.second, viewModelScope)
    }

    fun init(user: User, thumbnailsEnabled: Boolean) {
        val value = filter.value
        if (value == null || value.second != thumbnailsEnabled) {
            filter.value = user to thumbnailsEnabled
        }
    }
}