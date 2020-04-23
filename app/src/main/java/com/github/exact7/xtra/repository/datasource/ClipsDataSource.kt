package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.clip.Period
import java.util.concurrent.Executor

class ClipsDataSource(
        private val channelName: String?,
        private val gameName: String?,
        private val languages: String?,
        private val period: Period?,
        private val trending: Boolean?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePageKeyedDataSource<Clip>(retryExecutor) {

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        loadInitial(params, callback) {
            api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, null).execute().body()!!.let {
                it.clips to it.cursor
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Clip>) {
        loadAfter(params, callback) {
            api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, params.key).execute().body()!!.let {
                it.clips to it.cursor
            }
        }
    }

    class Factory(
            private val channelName: String?,
            private val gameName: String?,
            private val languages: String?,
            private val period: Period?,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<String, Clip, ClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
                ClipsDataSource(channelName, gameName, languages, period, trending, api, retryExecutor).also(sourceLiveData::postValue)
    }
}
