package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.clip.Clip
import java.util.concurrent.Executor

class FollowedClipsDataSource(
        userToken: String,
        private val trending: Boolean?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePageKeyedDataSource<Clip>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        loadInitial(params, callback) {
            api.getFollowedClips(userToken, trending, params.requestedLoadSize, null).execute().body()!!.let {
                it.clips to it.cursor
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Clip>) {
        loadAfter(params, callback) {
            api.getFollowedClips(userToken, trending, params.requestedLoadSize, params.key).execute().body()!!.let {
                it.clips to it.cursor
            }
        }
    }

    class Factory(
            private val userToken: String,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<String, Clip, FollowedClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
                FollowedClipsDataSource(userToken, trending, api, retryExecutor).also(sourceLiveData::postValue)
    }
}