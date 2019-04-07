package com.github.exact7.xtra.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.LoadingState
import io.reactivex.disposables.CompositeDisposable

abstract class PagedListViewModel<T> : ViewModel() {

    protected abstract val result: LiveData<Listing<T>>

    val list: LiveData<PagedList<T>> by lazy { switchMap(result) { it.pagedList } }
    val loadingState: LiveData<LoadingState> by lazy { switchMap(result) { it.loadingState } }
    val pagingState: LiveData<LoadingState> by lazy { switchMap(result) { it.pagingState } }

    protected val _loadedInitial by lazy {
        MediatorLiveData<Boolean?>().apply {
            addSource(loadingState) {
                if (value == null) {
                    if (it == LoadingState.LOADED) value = true
                    else if (it == LoadingState.FAILED) value = false
                }
            }
        }
    }
    val loadedInitial: LiveData<Boolean?>
        get() = _loadedInitial

    protected val compositeDisposable = CompositeDisposable()

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
