package com.github.exact7.xtra.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.LoadingState
import io.reactivex.disposables.CompositeDisposable

abstract class PagedListViewModel<T> : ViewModel() {

    private val result = MutableLiveData<Listing<T>>()
    val list: LiveData<PagedList<T>> = switchMap(result) { it.pagedList }
    val loadingState: LiveData<LoadingState> = switchMap(result) { it.loadingState }
    val pagingState: LiveData<LoadingState> = switchMap(result) { it.pagingState }
    private val _loadedInitial = MediatorLiveData<Boolean?>().apply {
        addSource(loadingState) {
            if (value == null) {
                if (it == LoadingState.LOADED) value = true
                else if (it == LoadingState.FAILED) value = false
            }
        }
    }
    val loadedInitial: LiveData<Boolean?>
        get() = _loadedInitial

    protected val compositeDisposable = CompositeDisposable()

    protected fun loadData(listing: Listing<T>, override: Boolean = false) {
        if (result.value == null || override) {
            result.value = listing
        }
    }

    fun refresh() {
        result.value?.refresh?.invoke()
    }

    fun retry() {
        result.value?.retry?.invoke()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
