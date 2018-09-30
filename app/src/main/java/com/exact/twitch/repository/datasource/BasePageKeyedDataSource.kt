package com.exact.twitch.repository.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import android.util.Log
import com.exact.twitch.repository.LoadingState
import java.util.concurrent.Executor

abstract class BasePageKeyedDataSource<T>(
        private val retryExecutor: Executor) : PageKeyedDataSource<String, T>(), PagingDataSource {

    protected val tag: String = javaClass.simpleName
    private var retry: (() -> Any)? = null

    override val loadingState = MutableLiveData<LoadingState>()
    override val pagingState = MutableLiveData<LoadingState>()

    override fun retry() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute { it.invoke() }
        }
    }

    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<String>, callback: PageKeyedDataSource.LoadInitialCallback<String, T>) {
        Log.d(tag, "Loading data. Size: " + params.requestedLoadSize)
        loadingState.postValue(LoadingState.LOADING)
    }

    override fun loadAfter(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, T>) {
        Log.d(tag, "Loading data. Size: " + params.requestedLoadSize)
        pagingState.postValue(LoadingState.LOADING)
    }

    override fun loadBefore(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, T>) {
    }

    protected fun PageKeyedDataSource.LoadInitialCallback<String, T>.onSuccess(data: List<T>, cursor: String) {
        this.onResult(data, 0, data.size, null, cursor)
        Log.d(tag, "Successfully loaded data")
        loadingState.postValue(LoadingState.LOADED)
        retry = null
    }

    protected fun PageKeyedDataSource.LoadCallback<String, T>.onSuccess(data: List<T>, cursor: String) {
        this.onResult(data, cursor)
        Log.d(tag, "Successfully loaded data")
        pagingState.postValue(LoadingState.LOADED)
        retry = null
    }

    protected fun PageKeyedDataSource.LoadInitialCallback<String, T>.onFailure(t: Throwable, params: PageKeyedDataSource.LoadInitialParams<String>) {
        Log.e(tag, "Error loading data: ${t.message}")
        t.printStackTrace()
        retry = { loadInitial(params, this) }
        loadingState.postValue(LoadingState.FAILED)
    }

    protected fun PageKeyedDataSource.LoadCallback<String, T>.onFailure(t: Throwable, params: PageKeyedDataSource.LoadParams<String>) {
        Log.e(tag, "Error loading data: ${t.message}")
        t.printStackTrace()
        retry = { loadAfter(params, this) }
        pagingState.postValue(LoadingState.FAILED)
    }
}