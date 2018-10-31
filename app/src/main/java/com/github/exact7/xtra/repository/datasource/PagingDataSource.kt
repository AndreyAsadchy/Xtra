package com.github.exact7.xtra.repository.datasource

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.repository.LoadingState

interface PagingDataSource {
    val loadingState: MutableLiveData<LoadingState>
    val pagingState: MutableLiveData<LoadingState>
    fun retry()
}
