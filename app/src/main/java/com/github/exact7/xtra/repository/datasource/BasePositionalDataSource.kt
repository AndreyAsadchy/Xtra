package com.github.exact7.xtra.repository.datasource

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.github.exact7.xtra.repository.LoadingState
import java.util.concurrent.Executor

abstract class BasePositionalDataSource<T>(private val retryExecutor: Executor) : PositionalDataSource<T>(), PagingDataSource {

    private val tag: String = javaClass.simpleName
    private var retry: (() -> Any)? = null

    override val loadingState = MutableLiveData<LoadingState>()
    override val pagingState = MutableLiveData<LoadingState>()

    protected fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>, request: () -> List<T>) {
        try {
            Log.d(tag, "Loading data. Size: " + params.requestedLoadSize)
            loadingState.postValue(LoadingState.LOADING)
            val data = request()
            callback.onResult(data, 0, data.size)
            Log.d(tag, "Successfully loaded data")
            loadingState.postValue(LoadingState.LOADED)
            retry = null
        } catch (e: Exception) {
            Log.e(tag, "Error loading data", e)
            e.printStackTrace()
            retry = { loadInitial(params, callback) }
            loadingState.postValue(LoadingState.FAILED)
        }
    }

    protected fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>, request: () -> List<T>) {
        try {
            Log.d(tag, "Loading data. Size: " + params.loadSize + " offset " + params.startPosition)
            pagingState.postValue(LoadingState.LOADING)
            val data = request()
            callback.onResult(data)
            Log.d(tag, "Successfully loaded data")
            pagingState.postValue(LoadingState.LOADED)
            retry = null
        } catch (e: Exception) {
            Log.e(tag, "Error loading data", e)
            e.printStackTrace()
            retry = { loadRange(params, callback) }
            pagingState.postValue(LoadingState.FAILED)
        }
    }

    override fun retry() {
        retry?.let {
            retryExecutor.execute { it.invoke() }
            retry = null
        }
    }
}
