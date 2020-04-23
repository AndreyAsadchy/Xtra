package com.github.exact7.xtra.ui.streams.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class StreamsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        repository.loadStreams(it?.game?.name, it.languages, it.streamType)
    }

    fun loadStreams(game: Game? = null, languages: String? = null) {
        Filter(game, languages).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
            val game: Game?,
            val languages: String?,
            val streamType: StreamType = StreamType.LIVE)
}
