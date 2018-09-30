package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.clip.Clip
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class FollowedClipsDataSource(
        userToken: String,
        private val trending: Boolean?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePageKeyedDataSource<Clip>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<String>, callback: PageKeyedDataSource.LoadInitialCallback<String, Clip>) {
        super.loadInitial(params, callback)
        api.getFollowedClips(userToken, trending, params.requestedLoadSize, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadAfter(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, Clip>) {
        super.loadAfter(params, callback)
        api.getFollowedClips(userToken, trending, params.requestedLoadSize, params.key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val userToken: String,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<String, Clip, FollowedClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
            FollowedClipsDataSource(userToken, trending, api, networkExecutor).also(sourceLiveData::postValue)
    }
}