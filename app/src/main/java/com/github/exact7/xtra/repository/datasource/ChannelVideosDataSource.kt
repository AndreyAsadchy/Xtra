package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.Video
import java.util.concurrent.Executor

class ChannelVideosDataSource (
        private val channelId: String,
        private val broadcastTypes: BroadcastType,
        private val sort: Sort,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Video>(retryExecutor) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            api.getChannelVideos(channelId, broadcastTypes, sort, params.requestedLoadSize, 0).execute().body()!!.videos
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            api.getChannelVideos(channelId, broadcastTypes, sort, params.loadSize, params.startPosition).execute().body()!!.videos
        }
    }

    class Factory(
            private val channelId: String,
            private val broadcastTypes: BroadcastType,
            private val sort: Sort,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<Int, Video, ChannelVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                ChannelVideosDataSource(channelId, broadcastTypes, sort, api, retryExecutor).also(sourceLiveData::postValue)
    }
}
