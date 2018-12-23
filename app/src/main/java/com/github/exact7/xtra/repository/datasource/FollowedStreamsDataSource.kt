package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class FollowedStreamsDataSource(
        userToken: String,
        private val streamType: StreamType,
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<Stream>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Stream>) {
        super.loadInitial(params, callback)
        api.getFollowedStreams(userToken, streamType, params.requestedLoadSize, 0)
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Stream>) {
        super.loadRange(params, callback)
        api.getFollowedStreams(userToken, streamType, params.loadSize, params.startPosition)
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val userToken: String,
            private val streamType: StreamType,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
            FollowedStreamsDataSource(userToken, streamType, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}