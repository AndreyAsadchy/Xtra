package com.github.exact7.xtra.repository

import com.github.exact7.xtra.db.RequestsDao
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao,
        private val requestsDao: RequestsDao) {

    fun loadAllVideos() = videosDao.getAll()

    fun getVideoByIdAsync(id: Int) = GlobalScope.async {
        videosDao.getById(id)
    }

    fun saveVideoAsync(video: OfflineVideo) = GlobalScope.async {
        videosDao.insert(video)
    }

    fun deleteVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.delete(video) }
    }

    fun updateVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.update(video) }
    }

    fun updateVideoPosition(id: Int, position: Long) {
        GlobalScope.launch { videosDao.updatePosition(id, position) }
    }

    fun getRequestsAsync() = GlobalScope.async {
        requestsDao.getAll()
    }

    fun saveRequest(request: Request) {
        GlobalScope.launch { requestsDao.insert(request) }
    }

    fun deleteRequest(request: Request) {
        GlobalScope.launch { requestsDao.delete(request) }
    }
}
