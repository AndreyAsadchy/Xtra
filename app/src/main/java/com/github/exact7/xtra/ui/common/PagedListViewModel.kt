package com.github.exact7.xtra.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.PagedList
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.LoadingState

abstract class PagedListViewModel<T> : BaseViewModel() {

    protected abstract val result: LiveData<Listing<T>>

    val list: LiveData<PagedList<T>> by lazy { switchMap(result) { it.pagedList } }
    val loadingState: LiveData<LoadingState> by lazy { switchMap(result) { it.loadingState } }
    val pagingState: LiveData<LoadingState> by lazy { switchMap(result) { it.pagingState } }

    fun refresh() {
        result.value?.refresh?.invoke()
    }

    fun retry() {
        result.value?.retry?.invoke()
    }
}
