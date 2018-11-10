package com.github.exact7.xtra.ui.streams.followed

import com.github.exact7.xtra.model.stream.Stream
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.streams.StreamType
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(val repository: TwitchService) : PagedListViewModel<Stream>() {

    private lateinit var userToken: String

    fun setUserToken(userToken: String) {
        if (this.userToken != userToken) {
            this.userToken = userToken
            loadData(repository.loadFollowedStreams(userToken, StreamType.ALL, compositeDisposable))
        }
    }
}