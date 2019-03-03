package com.github.exact7.xtra.ui.download

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.util.DownloadUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ClipDownloadViewModel @Inject constructor(
        application: Application,
        private val playerRepository: PlayerRepository,
        private val offlineRepository: OfflineRepository
) : AndroidViewModel(application) {

    private val _qualities = MutableLiveData<Map<String, String>>()
    val qualities: LiveData<Map<String, String>>
        get() = _qualities

    private val compositeDisposable = CompositeDisposable()
    lateinit var clip: Clip

    fun setQualities(qualities: Map<String, String>?) {
        if (qualities == null) {
            playerRepository.fetchClipQualities(clip.slug)
                    .subscribe({
                        setQualities(it)
                    }, {

                    })
                    .addTo(compositeDisposable)
        } else {
            _qualities.value = qualities
        }
    }

    fun download(url: String, quality: String) {
        GlobalScope.launch {
            val context = getApplication<Application>()
            val path = context.getExternalFilesDir(".downloads${File.separator}${clip.slug}$quality")!!.absolutePath
            val offlineVideo = DownloadUtils.prepareDownload(context, clip, path, clip.duration.toLong())
            val videoId = offlineRepository.saveVideo(offlineVideo)
            DownloadUtils.download(context, ClipRequest(videoId.toInt(), url, offlineVideo.url))
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}