package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.clip.Clip

class FollowedClipsDataSource(
        userToken: String,
        private val trending: Boolean?,
        private val api: KrakenApi) : BasePageKeyedDataSource<Clip>() {

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
            private val api: KrakenApi) : BaseDataSourceFactory<String, Clip, FollowedClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
            FollowedClipsDataSource(userToken, trending, api).also(sourceLiveData::postValue)
    }
}