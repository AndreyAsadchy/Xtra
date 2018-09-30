package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.stream.Stream
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class StreamsDataSource private constructor(
        private val game: String?,
        private val languages: String?,
        private val streamType: String?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Stream>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Stream>) {
        super.loadInitial(params, callback)
        api.getStreams(game, languages, streamType, params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Stream>) {
        super.loadRange(params, callback)
        api.getStreams(game, languages, streamType, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val game: String?,
            private val languages: String?,
            private val streamType: String?,
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
            StreamsDataSource(game, languages, streamType, api, networkExecutor).also(sourceLiveData::postValue)
    }
}
