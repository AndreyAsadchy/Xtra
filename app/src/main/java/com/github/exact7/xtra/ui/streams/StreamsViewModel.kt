package com.github.exact7.xtra.ui.streams

import com.github.exact7.xtra.model.stream.Stream
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class StreamsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Stream>() {

    fun loadStreams(game: String? = null, languages: String? = null, streamType: StreamType = StreamType.LIVE) {
        loadData(repository.loadStreams(game, languages, streamType, compositeDisposable))
    }

    fun loadFollowedStreams(userToken: String, streamType: StreamType = StreamType.ALL) {
        loadData(repository.loadFollowedStreams(userToken, streamType, compositeDisposable))
    }
}
