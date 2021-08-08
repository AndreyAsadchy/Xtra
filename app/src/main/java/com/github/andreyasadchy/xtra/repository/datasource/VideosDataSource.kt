package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.video.BroadcastType
import com.github.andreyasadchy.xtra.model.kraken.video.Period
import com.github.andreyasadchy.xtra.model.kraken.video.Sort
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import kotlinx.coroutines.CoroutineScope

class VideosDataSource private constructor(
        private val game: String?,
        private val period: Period,
        private val broadcastTypes: BroadcastType,
        private val language: String?,
        private val sort: Sort,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            api.getTopVideos(game, period, broadcastTypes, language, sort, params.requestedLoadSize, 0).videos
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            api.getTopVideos(game, period, broadcastTypes, language, sort, params.loadSize, params.startPosition).videos
        }
    }

    class Factory (
            private val game: String?,
            private val period: Period,
            private val broadcastTypes: BroadcastType,
            private val language: String?,
            private val sort: Sort,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, VideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                VideosDataSource(game, period, broadcastTypes, language, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
