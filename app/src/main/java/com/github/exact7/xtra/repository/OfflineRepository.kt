package com.github.exact7.xtra.repository

import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.model.offline.OfflineVideo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao) {

    fun loadAllVideos() = videosDao.getAll()

    fun saveVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.insert(video) }
    }

    fun deleteVideo(video: OfflineVideo) {
        GlobalScope.launch { videosDao.delete(video) }
    }
//
//    fun saveRequest(request: VideoRequest) {
//        GlobalScope.launch { videoRequestsDao.insert(request) }
//    }
//
//    fun getRequest(id: Int) = videoRequestsDao.get(id)
//
//    fun deleteRequest(request: VideoRequest) {
//        GlobalScope.launch { videoRequestsDao.delete(request) }
//    }
}
