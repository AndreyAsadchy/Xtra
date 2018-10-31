package com.github.exact7.xtra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.github.exact7.xtra.repository.datasource.BaseDataSourceFactory
import com.github.exact7.xtra.repository.datasource.PagingDataSource
import java.util.concurrent.Executor

class Listing<T> internal constructor(
        val pagedList: LiveData<PagedList<T>>,
        val loadingState: LiveData<LoadingState>,
        val pagingState: LiveData<LoadingState>,
        val refresh: () -> Unit,
        val retry: () -> Unit) {

    companion object {

        fun <ListValue, DS> create(factory: BaseDataSourceFactory<*, ListValue, DS>, config: PagedList.Config, executor: Executor): Listing<ListValue> where DS : DataSource<*, ListValue>, DS : PagingDataSource {
            val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()
            val loadingState = Transformations.switchMap(factory.sourceLiveData) { it.loadingState }
            val pagingState = Transformations.switchMap(factory.sourceLiveData) { it.pagingState }
            return Listing(
                    pagedList,
                    loadingState,
                    pagingState,
                    refresh = { factory.sourceLiveData.value?.invalidate() },
                    retry = { factory.sourceLiveData.value?.retry() })
        }
    }
}
