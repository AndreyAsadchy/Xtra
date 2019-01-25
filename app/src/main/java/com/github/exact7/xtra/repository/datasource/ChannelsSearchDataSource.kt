package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.channel.Channel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class ChannelsSearchDataSource private constructor(
        private val query: String,
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<Channel>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Channel>) {
        super.loadInitial(params, callback)
        api.getChannels(query, params.requestedLoadSize, 0)
                .subscribe({ callback.onSuccess(it.channels) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Channel>) {
        super.loadRange(params, callback)
        api.getChannels(query, params.loadSize, params.startPosition)
                .subscribe({ callback.onSuccess(it.channels) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val query: String,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(query, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
