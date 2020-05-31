package com.github.exact7.xtra.ui.download

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.offline.Request
import com.github.exact7.xtra.repository.GraphQLRepositoy
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.TwitchApiHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ClipDownloadViewModel @Inject constructor(
        application: Application,
        private val graphQLRepositoy: GraphQLRepositoy,
        private val offlineRepository: OfflineRepository
) : AndroidViewModel(application) {

    private val _qualities = MutableLiveData<Map<String, String>>()
    val qualities: LiveData<Map<String, String>>
        get() = _qualities

    private lateinit var clip: Clip

    fun init(clip: Clip, qualities: Map<String, String>?) {
        if (!this::clip.isInitialized) {
            this.clip = clip
            if (qualities == null) {
                viewModelScope.launch {
                    try {
                        val urls = graphQLRepositoy.loadClipUrls(clip.slug)
                        _qualities.postValue(urls)
                    } catch (e: Exception) {

                    }
                }
            } else {
                _qualities.value = qualities
            }
        }
    }

    fun download(url: String, path: String, quality: String) {
        GlobalScope.launch {
            val context = getApplication<Application>()

            val filePath = "$path${File.separator}${clip.slug}$quality"
            val startPosition = clip.vod?.let { TwitchApiHelper.parseClipOffset(it.url) }?.toLong()

            val offlineVideo = DownloadUtils.prepareDownload(context, clip, url, filePath, clip.duration.toLong(), startPosition)
            val videoId = offlineRepository.saveVideo(offlineVideo).toInt()
            val request = Request(videoId, url, offlineVideo.url)
            offlineRepository.saveRequest(request)

            DownloadUtils.download(context, request)
        }
    }
}