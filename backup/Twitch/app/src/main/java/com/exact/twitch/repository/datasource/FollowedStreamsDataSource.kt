package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.stream.Stream
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class FollowedStreamsDataSource(
        userToken: String,
        private val streamType: String?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Stream>(retryExecutor) {

    private val userToken: String = "OAuth $userToken"

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Stream>) {
        super.loadInitial(params, callback)
        api.getFollowedStreams(userToken, streamType, params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Stream>) {
        super.loadRange(params, callback)
        api.getFollowedStreams(userToken, streamType, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.streams) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val userToken: String,
            private val streamType: String?,
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
            FollowedStreamsDataSource(userToken, streamType, api, networkExecutor).also(sourceLiveData::postValue)
    }
}