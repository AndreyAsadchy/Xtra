package com.github.andreyasadchy.xtra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.PagingDataSource

class Listing<T> internal constructor(
        val pagedList: LiveData<PagedList<T>>,
        val loadingState: LiveData<LoadingState>,
        val pagingState: LiveData<LoadingState>,
        val refresh: () -> Unit,
        val retry: () -> Unit) {

    companion object {

        fun <ListValue, DS> create(factory: BaseDataSourceFactory<*, ListValue, DS>, config: PagedList.Config): Listing<ListValue> where DS : DataSource<*, ListValue>, DS : PagingDataSource {
            val pagedList = LivePagedListBuilder(factory, config).build()
            val loadingState = factory.sourceLiveData.switchMap { it.loadingState }
            val pagingState = factory.sourceLiveData.switchMap { it.pagingState }
            return Listing(
                    pagedList,
                    loadingState,
                    pagingState,
                    refresh = { factory.sourceLiveData.value?.invalidate() },
                    retry = { factory.sourceLiveData.value?.retry() })
        }
    }
}
