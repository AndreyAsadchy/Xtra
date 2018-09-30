package com.exact.twitch.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.exact.twitch.db.AppDatabase
import com.exact.twitch.db.VideosDao
import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.repository.datasource.OfflineVideosDataSource
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao) {
//        private val executor: Executor) {

    fun loadAll(): LiveData<PagedList<OfflineVideo>> =
            LivePagedListBuilder(videosDao.getAll(), 15).build()

//    fun loadDownloadedVideos(): Listing<OfflineVideo> {
//        val factory = OfflineVideosDataSource.Factory(videosDao, executor)
//        val config = PagedList.Config.Builder()
//                .setPageSize(10)
//                .setInitialLoadSizeHint(15)
//                .setPrefetchDistance(3)
//                .setEnablePlaceholders(false)
//                .build() //TODO maybe paging is already here?
//        // The Int type parameter tells Room to use a PositionalDataSource
//        //    // object, with position-based loading under the hood.
//        return Listing.create(factory, config, executor)
//    }

    fun insert(video: OfflineVideo) {
        launch { videosDao.insert(video) }
    }

    fun delete(video: OfflineVideo) {
        launch { videosDao.delete(video) }
    }
}
