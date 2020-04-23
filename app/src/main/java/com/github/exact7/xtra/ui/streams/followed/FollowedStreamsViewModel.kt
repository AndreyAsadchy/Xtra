package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val user = MutableLiveData<User>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(user) {
        repository.loadFollowedStreams(it.token, StreamType.ALL)
    }

    fun setUser(user: User) {
        if (this.user.value == null) {
            this.user.value  = user
        }
    }
}