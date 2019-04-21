package com.github.exact7.xtra.repository

import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.offline.OfflineVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao) {

    fun loadAllVideos() = videosDao.getAll()

    suspend fun getVideoById(id: Int) = withContext(Dispatchers.Default) {
        videosDao.getById(id)
    }

    suspend fun saveVideo(video: OfflineVideo) = withContext(Dispatchers.Default) {
        videosDao.insert(video)
    }

    fun deleteVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.delete(video) }
    }

    fun updateVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.update(video) }
    }

    suspend fun getUnfinishedVideos() = withContext(Dispatchers.Default) {
        videosDao.getUnfinishedVideos()
    }
}
