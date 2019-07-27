package com.github.exact7.xtra.ui.search.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import javax.inject.Inject

class ChannelSearchViewModel @Inject constructor(
        private val repository: TwitchService) : BaseViewModel() {

    private val query = MutableLiveData<String>()
    private val result: LiveData<Listing<Channel>> = Transformations.map(query) {
        repository.loadChannels(it, compositeDisposable)
    }
    val list: LiveData<PagedList<Channel>> = Transformations.switchMap(result) { it.pagedList }
    val loadingState: LiveData<LoadingState> = Transformations.switchMap(result) { it.loadingState }

    fun setQuery(query: String) {
        if (this.query.value != query) {
            this.query.value = query
        }
    }
}