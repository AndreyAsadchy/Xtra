package com.exact.twitch.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.exact.twitch.repository.Listing
import com.exact.twitch.repository.LoadingState
import io.reactivex.disposables.CompositeDisposable

abstract class PagedListViewModel<T> : ViewModel() {

    private val result = MutableLiveData<Listing<T>>()
    lateinit var list: LiveData<PagedList<T>>
        private set
    lateinit var loadingState: LiveData<LoadingState>
        private set
    lateinit var pagingState: LiveData<LoadingState>
        private set
    val loadedInitial = MutableLiveData<Boolean>()
    private val initialObserver = Observer<LoadingState> {
        if (loadedInitial.value == null && it != LoadingState.LOADING)
            loadedInitial.postValue(true)
    }

    protected val compositeDisposable = CompositeDisposable()

    internal fun loadData(listing: Listing<T>, override: Boolean = false) {
        if (result.value == null || override) {
            result.value = listing
            list = switchMap(result) { it.pagedList }
            loadingState = switchMap(result) { it.loadingState }
            pagingState = switchMap(result) { it.pagingState }
            loadingState.observeForever(initialObserver)
        }
    }

    fun refresh() {
        result.value?.refresh?.invoke()
    }

    fun retry() {
        result.value?.retry?.invoke()
    }

    override fun onCleared() {
        super.onCleared()
        loadingState.removeObserver(initialObserver)
        compositeDisposable.clear()
    }
}
