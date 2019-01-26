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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val repository: TwitchService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private var job: Job? = null
    private val _query = MutableLiveData<String>()
    var query = ""
        set(value) {
            field = value
            job?.cancel()
            job = GlobalScope.launch {
                delay(500)
                _query.postValue(query)
            }
        }
    private val result: LiveData<Listing<Channel>> = Transformations.map(_query) {
        repository.loadChannels(it, compositeDisposable)
    }
    val list: LiveData<PagedList<Channel>> = Transformations.switchMap(result) { it.pagedList }
    val loadingState: LiveData<LoadingState> = Transformations.switchMap(result) { it.loadingState }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}