package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.ui.streams.StreamType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor

class StreamsDataSource private constructor(
        private val game: String?,
        private val languages: String?,
        private val streamType: StreamType,
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<Stream>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Stream>) {
        super.loadInitial(params, callback)
        api.getStreams(game, languages, streamType, params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Stream>) {
        super.loadRange(params, callback)
        api.getStreams(game, languages, streamType, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val game: String?,
            private val languages: String?,
            private val streamType: StreamType,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSource(game, languages, streamType, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
