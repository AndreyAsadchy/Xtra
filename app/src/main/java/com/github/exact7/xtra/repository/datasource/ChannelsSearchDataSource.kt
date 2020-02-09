package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.channel.Channel

class ChannelsSearchDataSource private constructor(
        private val query: String,
        private val api: KrakenApi) : BasePositionalDataSource<Channel>() {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        super.loadInitial(params, callback)
        api.getChannels(query, params.requestedLoadSize, 0)
                .subscribe({ callback.onSuccess(it.channels) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        super.loadRange(params, callback)
        api.getChannels(query, params.loadSize, params.startPosition)
                .subscribe({ callback.onSuccess(it.channels) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val query: String,
            private val api: KrakenApi) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(query, api).also(sourceLiveData::postValue)
    }
}
