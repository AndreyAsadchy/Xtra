package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.ui.streams.StreamType
import io.reactivex.android.schedulers.AndroidSchedulers
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Stream>) {
        super.loadRange(params, callback)
        api.getFollowedStreams(userToken, streamType, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
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