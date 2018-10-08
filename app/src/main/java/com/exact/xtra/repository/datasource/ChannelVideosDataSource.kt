package com.exact.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.xtra.api.KrakenApi
import com.exact.xtra.model.video.Video
import com.exact.xtra.ui.videos.BroadcastType
import com.exact.xtra.ui.videos.Sort
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class ChannelVideosDataSource (
        private val channelId: Any,
        private val broadcastTypes: BroadcastType,
        private val sort: Sort,
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<Video>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Video>) {
        super.loadInitial(params, callback)
        api.getChannelVideos(channelId, broadcastTypes, sort, params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.videos) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Video>) {
        super.loadRange(params, callback)
        api.getChannelVideos(channelId, broadcastTypes, sort, params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.videos) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val channelId: Any,
            private val broadcastTypes: BroadcastType,
            private val sort: Sort,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, Video, ChannelVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                ChannelVideosDataSource(channelId, broadcastTypes, sort, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
