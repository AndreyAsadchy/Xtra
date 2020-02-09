package com.github.exact7.xtra.repository.datasource

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.github.exact7.xtra.repository.LoadingState

abstract class BasePositionalDataSource<T> : PositionalDataSource<T>(), PagingDataSource {

    protected val tag: String = javaClass.simpleName
    private var retry: (() -> Any)? = null

    override val loadingState = MutableLiveData<LoadingState>()
    override val pagingState = MutableLiveData<LoadingState>()

    override fun retry() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
//            retryExecutor.execute { it.invoke() }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        Log.d(tag, "Loading data. Size: " + params.requestedLoadSize)
        loadingState.postValue(LoadingState.LOADING)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        Log.d(tag, "Loading data. Size: " + params.loadSize + " offset " + params.startPosition)
        pagingState.postValue(LoadingState.LOADING)
    }

    protected fun PositionalDataSource.LoadInitialCallback<T>.onSuccess(data: List<T>) {
        this.onResult(data, 0, data.size)
        Log.d(tag, "Successfully loaded data")
        loadingState.postValue(LoadingState.LOADED)
        retry = null
    }

    protected fun PositionalDataSource.LoadRangeCallback<T>.onSuccess(data: List<T>) {
        this.onResult(data)
        Log.d(tag, "Successfully loaded data")
        pagingState.postValue(LoadingState.LOADED)
        retry = null
    }

    protected fun PositionalDataSource.LoadInitialCallback<T>.onFailure(t: Throwable, params: PositionalDataSource.LoadInitialParams) {
        Log.e(tag, "Error finished data: ${t.message}")
        t.printStackTrace()
        retry = { loadInitial(params, this) }
        loadingState.postValue(LoadingState.FAILED)
    }

    protected fun PositionalDataSource.LoadRangeCallback<T>.onFailure(t: Throwable, params: PositionalDataSource.LoadRangeParams) {
        Log.e(tag, "Error finished data: ${t.message}")
        t.printStackTrace()
        retry = { loadRange(params, this) }
        pagingState.postValue(LoadingState.FAILED)
    }
}
