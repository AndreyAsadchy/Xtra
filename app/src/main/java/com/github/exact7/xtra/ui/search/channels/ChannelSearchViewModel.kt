package com.github.exact7.xtra.ui.search.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class ChannelSearchViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Channel>() {

    private val query = MutableLiveData<String>()
    override val result: LiveData<Listing<Channel>> = Transformations.map(query) {
        repository.loadChannels(it, viewModelScope)
    }

    fun setQuery(query: String) {
        if (this.query.value != query) {
            this.query.value = query
        }
    }
}