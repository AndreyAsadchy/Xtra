package com.github.exact7.xtra.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.repository.TwitchService
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val repository: TwitchService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _query = MutableLiveData<String>()
    private val result: LiveData<Listing<Channel>> = Transformations.map(_query) {
        repository.loadChannels(it, compositeDisposable)
    }
    val list: LiveData<PagedList<Channel>> = Transformations.switchMap(result) { it.pagedList }
    val loadingState: LiveData<LoadingState> = Transformations.switchMap(result) { it.loadingState }

    fun setQuery(query: String) {
        println("set $query")
        _query.value = query
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}