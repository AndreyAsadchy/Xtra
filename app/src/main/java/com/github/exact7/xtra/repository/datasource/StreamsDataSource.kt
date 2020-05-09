package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import kotlinx.coroutines.CoroutineScope

class StreamsDataSource private constructor(
        private val game: String?,
        private val languages: String?,
        private val streamType: StreamType,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            api.getStreams(game, languages, streamType, params.requestedLoadSize, 0).streams
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            api.getStreams(game, languages, streamType, params.loadSize, params.startPosition).streams
        }
    }

    class Factory(
            private val game: String?,
            private val languages: String?,
            private val streamType: StreamType,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSource(game, languages, streamType, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
