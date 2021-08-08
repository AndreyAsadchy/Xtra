package com.github.andreyasadchy.xtra.repository

import android.content.Context
import com.github.andreyasadchy.xtra.db.RequestsDao
import com.github.andreyasadchy.xtra.db.VideosDao
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.model.offline.Request
import com.github.andreyasadchy.xtra.ui.download.DownloadService
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
        private val videosDao: VideosDao,
        private val requestsDao: RequestsDao) {

    fun loadAllVideos() = videosDao.getAll()

    suspend fun getVideoById(id: Int) = withContext(Dispatchers.IO) {
        videosDao.getById(id)
    }

    suspend fun saveVideo(video: OfflineVideo) = withContext(Dispatchers.IO) {
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

    fun resumeDownloads(context: Context, wifiOnly: Boolean) {
        GlobalScope.launch {
            requestsDao.getAll().forEach {
                if (DownloadService.activeRequests.add(it.offlineVideoId)) {
                    DownloadUtils.download(context, it, wifiOnly)
                }
            }
        }
    }

    fun saveRequest(request: Request) {
        GlobalScope.launch { requestsDao.insert(request) }
    }

    fun deleteRequest(request: Request) {
        GlobalScope.launch { requestsDao.delete(request) }
    }
}
