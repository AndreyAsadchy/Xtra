package com.exact.twitch.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList

import com.exact.twitch.repository.Listing
import com.exact.twitch.repository.LoadingState
import com.exact.twitch.repository.TwitchService

import android.arch.lifecycle.Transformations.switchMap

abstract class PagedListViewModel<T> internal constructor(protected val repository: TwitchService)//TODO change
    : ListViewModel<PagedList<T>>() {
    private val result = MutableLiveData<Listing<T>>()
    var loadingState: LiveData<LoadingState>? = null
        private set
    var refreshState: LiveData<LoadingState>? = null
        private set

    internal fun loadData(listing: Listing<T>, override: Boolean) {
        if (result.value == null || override) {
            result.postValue(listing)
            list = switchMap(result, ???({ it.getPagedList() }))
            loadingState = switchMap(result, ???({ it.getNetworkState() }))
            refreshState = switchMap(result, ???({ it.getRefreshState() }))
        }
    }

    fun refresh() {
        val listing = result.value
        listing?.refresh?.run()
    }

    fun retry() {
        val listing = result.value
        listing?.retry?.run()
    }
}
