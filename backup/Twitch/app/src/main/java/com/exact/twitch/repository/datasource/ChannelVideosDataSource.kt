package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.video.Video
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class ChannelVideosDataSource (
        private val channelId: Any,
        private val broadcastTypes: String?,
        private val sort: String?,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Video>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Video>) {
        super.loadInitial(params, callback)
        api.getChannelVideos(channelId, broadcastTypes, sort, params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.videos) }, { callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Video>) {
        super.loadRange(params, callback)
        api.getChannelVideos(channelId, broadcastTypes, sort, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.videos) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val channelId: Any,
            private val broadcastTypes: String?,
            private val sort: String?,
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<Int, Video, ChannelVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                ChannelVideosDataSource(channelId, broadcastTypes, sort, api, networkExecutor).also(sourceLiveData::postValue)
    }
}
