package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.stream.Stream
import com.github.andreyasadchy.xtra.model.kraken.stream.StreamType
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
        userToken: String,
        private val streamType: StreamType,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {

    private val userToken: String = TwitchApiHelper.addTokenPrefix(userToken)

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            api.getFollowedStreams(userToken, streamType, params.requestedLoadSize, 0).streams
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            api.getFollowedStreams(userToken, streamType, params.loadSize, params.startPosition).streams
        }
    }

    class Factory(
            private val userToken: String,
            private val streamType: StreamType,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(userToken, streamType, api, coroutineScope).also(sourceLiveData::postValue)
    }
}