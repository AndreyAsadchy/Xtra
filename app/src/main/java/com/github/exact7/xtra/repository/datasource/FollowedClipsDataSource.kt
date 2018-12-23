package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.clip.Clip
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class FollowedClipsDataSource(
        userToken: String,
        private val trending: Boolean?,
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePageKeyedDataSource<Clip>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<String>, callback: PageKeyedDataSource.LoadInitialCallback<String, Clip>) {
        super.loadInitial(params, callback)
        api.getFollowedClips(userToken, trending, params.requestedLoadSize, null)
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadAfter(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, Clip>) {
        super.loadAfter(params, callback)
        api.getFollowedClips(userToken, trending, params.requestedLoadSize, params.key)
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val userToken: String,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<String, Clip, FollowedClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
            FollowedClipsDataSource(userToken, trending, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}