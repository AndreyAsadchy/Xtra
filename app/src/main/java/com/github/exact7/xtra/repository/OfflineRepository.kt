package com.github.exact7.xtra.repository

import com.github.exact7.xtra.db.ClipRequestsDao
import com.github.exact7.xtra.db.VideoRequestsDao
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.VideoRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao,
        private val videoRequestsDao: VideoRequestsDao,
        private val clipRequestsDao: ClipRequestsDao) {

    fun loadAllVideos() = videosDao.getAll()

    fun saveVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.insert(video) }
    }

    fun deleteVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.delete(video) }
    }

    suspend fun getVideoRequest(id: Int) = GlobalScope.async {
        videoRequestsDao.get(id)
    }.await()

    suspend fun getClipRequest(id: Int) = GlobalScope.async {
        clipRequestsDao.get(id)
    }.await()

    suspend fun saveRequest(request: VideoRequest) = GlobalScope.async {
        videoRequestsDao.insert(request)
    }.await()

    suspend fun saveRequest(request: ClipRequest) = GlobalScope.async {
        clipRequestsDao.insert(request)
    }.await()

    fun deleteRequest(request: VideoRequest) {
        GlobalScope.launch { videoRequestsDao.delete(request) }
    }

    fun deleteRequest(request: ClipRequest) {
        GlobalScope.launch { clipRequestsDao.delete(request) }
    }
}
