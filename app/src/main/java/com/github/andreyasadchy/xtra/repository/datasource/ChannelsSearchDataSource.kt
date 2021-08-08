package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.channel.Channel
import kotlinx.coroutines.CoroutineScope

class ChannelsSearchDataSource private constructor(
        private val query: String,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Channel>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        loadInitial(params, callback) {
            api.getChannels(query, params.requestedLoadSize, 0).channels
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        loadRange(params, callback) {
            api.getChannels(query, params.loadSize, params.startPosition).channels
        }
    }

    class Factory(
            private val query: String,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(query, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
