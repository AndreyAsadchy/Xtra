package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.db.VideosDao
import com.exact.twitch.model.OfflineVideo
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class OfflineVideosDataSource(
        private val videosDao: VideosDao,
        retryExecutor: Executor) : BasePositionalDataSource<OfflineVideo>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<OfflineVideo>) {
        super.loadInitial(params, callback)
        videosDao.getAfter(0, params.requestedLoadSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it)}, { callback.onFailure(it, params)} )
                .dispose()
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<OfflineVideo>) {
        super.loadRange(params, callback)
        videosDao.getAfter(params.startPosition, params.loadSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it)}, { callback.onFailure(it, params)} )
                .dispose()
    }

    class Factory(
            private val videosDao: VideosDao,
            private val networkExecutor: Executor) : BaseDataSourceFactory<Int, OfflineVideo, OfflineVideosDataSource>() {

        override fun create(): DataSource<Int, OfflineVideo> =
            OfflineVideosDataSource(videosDao, networkExecutor).also(sourceLiveData::postValue)
    }
}
