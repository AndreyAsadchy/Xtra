package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import java.util.concurrent.Executor

class FollowedStreamsDataSource(
        userToken: String,
        private val streamType: StreamType,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Stream>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            api.getFollowedStreams(userToken, streamType, params.requestedLoadSize, 0).execute().body()!!.streams
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            api.getFollowedStreams(userToken, streamType, params.loadSize, params.startPosition).execute().body()!!.streams
        }
    }

    class Factory(
            private val userToken: String,
            private val streamType: StreamType,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(userToken, streamType, api, retryExecutor).also(sourceLiveData::postValue)
    }
}