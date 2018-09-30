package com.exact.twitch.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.exact.twitch.repository.datasource.BaseDataSourceFactory
import com.exact.twitch.repository.datasource.PagingDataSource
import java.util.concurrent.Executor

class Listing<T> internal constructor(
        val pagedList: LiveData<PagedList<T>>,
        val networkState: LiveData<LoadingState>,
        val refreshState: LiveData<LoadingState>,
        val refresh: () -> Unit,
        val retry: () -> Unit) {

    companion object {

        fun <ListValue, DS> create(factory: BaseDataSourceFactory<*, ListValue, DS>, config: PagedList.Config, executor: Executor): Listing<ListValue> where DS : DataSource<*, ListValue>, DS : PagingDataSource {
            val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()
            val networkState = Transformations.switchMap(factory.sourceLiveData) { it.loadingState }
            val refreshState = Transformations.switchMap(factory.sourceLiveData) { it.initialLoadingState }
            val dataSource = factory.sourceLiveData.value
            val refresh = { dataSource?.invalidate()!! }
            val retry = { dataSource?.retry()!! }
            return Listing(pagedList, networkState, refreshState, refresh, retry)
        }
    }
}
