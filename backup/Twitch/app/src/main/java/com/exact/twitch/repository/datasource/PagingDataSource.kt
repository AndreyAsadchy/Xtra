package com.exact.twitch.repository.datasource

import androidx.lifecycle.MutableLiveData
import com.exact.twitch.repository.LoadingState

interface PagingDataSource {
    val loadingState: MutableLiveData<LoadingState>
    val initialLoadingState: MutableLiveData<LoadingState>
    fun retry()
}
