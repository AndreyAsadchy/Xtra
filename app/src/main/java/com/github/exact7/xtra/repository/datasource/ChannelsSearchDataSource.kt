package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.channel.Channel
import java.util.concurrent.Executor

class ChannelsSearchDataSource private constructor(
        private val query: String,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Channel>(retryExecutor) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        loadInitial(params, callback) {
            api.getChannels(query, params.requestedLoadSize, 0).execute().body()!!.channels
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        loadRange(params, callback) {
            api.getChannels(query, params.loadSize, params.startPosition).execute().body()!!.channels
        }
    }

    class Factory(
            private val query: String,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(query, api, retryExecutor).also(sourceLiveData::postValue)
    }
}
