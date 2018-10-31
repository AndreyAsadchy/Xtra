package com.github.exact7.xtra.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.OfflineVideo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao) {

    fun loadAll(): LiveData<PagedList<OfflineVideo>> =
            LivePagedListBuilder(videosDao.getAll(), 15).build()

    fun insert(video: OfflineVideo) {
        GlobalScope.launch { videosDao.insert(video) }
    }

    fun delete(video: OfflineVideo) {
        GlobalScope.launch { videosDao.delete(video) }
    }
}
