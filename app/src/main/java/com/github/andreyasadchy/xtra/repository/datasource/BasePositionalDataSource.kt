package com.github.andreyasadchy.xtra.repository.datasource

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.github.andreyasadchy.xtra.repository.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class BasePositionalDataSource<T>(private val coroutineScope: CoroutineScope) : PositionalDataSource<T>(), PagingDataSource {

    private val tag: String = javaClass.simpleName
    private var retry: (() -> Any)? = null

    override val loadingState = MutableLiveData<LoadingState>()
    override val pagingState = MutableLiveData<LoadingState>()

    protected fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>, request: suspend () -> List<T>) {
        runBlocking {
            coroutineScope.launch(Dispatchers.IO) {
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
                    retry = { loadInitial(params, callback, request) }
                    loadingState.postValue(LoadingState.FAILED)
                }
            }.join()
        }
    }

    protected fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>, request: suspend () -> List<T>) {
        runBlocking {
            coroutineScope.launch(Dispatchers.IO) {
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
                    retry = { loadRange(params, callback, request) }
                    pagingState.postValue(LoadingState.FAILED)
                }
            }.join()
        }
    }

    override fun retry() {
        retry?.let {
            coroutineScope.launch(Dispatchers.IO) {
                it.invoke()
            }
            retry = null
        }
    }
}
