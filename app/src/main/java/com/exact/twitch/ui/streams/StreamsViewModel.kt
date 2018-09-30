package com.exact.twitch.ui.streams

import com.exact.twitch.model.stream.Stream
import com.exact.twitch.repository.TwitchService
import com.exact.twitch.ui.common.PagedListViewModel
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
