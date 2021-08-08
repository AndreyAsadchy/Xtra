package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.video.BroadcastType
import com.github.andreyasadchy.xtra.model.kraken.video.Sort
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope

class FollowedVideosDataSource(
        userToken: String,
        private val broadcastTypes: BroadcastType,
        private val language: String?,
        private val sort: Sort,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {

    private val userToken: String = TwitchApiHelper.addTokenPrefix(userToken)

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            api.getFollowedVideos(userToken, broadcastTypes, language, sort, params.requestedLoadSize, 0).videos
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            api.getFollowedVideos(userToken, broadcastTypes, language, sort, params.loadSize, params.startPosition).videos
        }
    }

    class Factory(
            private val userToken: String,
            private val broadcastTypes: BroadcastType,
            private val language: String?,
            private val sort: Sort,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, FollowedVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                FollowedVideosDataSource(userToken, broadcastTypes, language, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
