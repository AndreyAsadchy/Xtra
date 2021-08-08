package com.github.andreyasadchy.xtra.repository.datasource

import androidx.lifecycle.MutableLiveData
import com.github.andreyasadchy.xtra.repository.LoadingState

interface PagingDataSource {
    val loadingState: MutableLiveData<LoadingState>
    val pagingState: MutableLiveData<LoadingState>
    fun retry()
}
