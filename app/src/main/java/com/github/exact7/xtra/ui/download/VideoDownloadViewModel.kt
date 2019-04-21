package com.github.exact7.xtra.ui.download

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.VideoDownloadInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.util.DownloadUtils
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.ParsingMode
import com.iheartradio.m3u8.PlaylistParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import javax.inject.Inject

class VideoDownloadViewModel @Inject constructor(
        application: Application,
        private val playerRepository: PlayerRepository,
        private val offlineRepository: OfflineRepository
) : AndroidViewModel(application) {

    private val _videoInfo = MutableLiveData<VideoDownloadInfo?>()
    val videoInfo: LiveData<VideoDownloadInfo?>
        get() = _videoInfo

    private val compositeDisposable = CompositeDisposable()

    fun setVideo(video: Video) {
        if (_videoInfo.value == null) {
            playerRepository.loadVideoPlaylist(video.id)
                    .map { response ->
                        if (response.isSuccessful) {
                            val playlist = response.body()!!.string()
                            val qualities = "NAME=\"(.*)\"".toRegex().findAll(playlist).map { it.groupValues[1] }.toMutableList()
                            val urls = "https://.*\\.m3u8".toRegex().findAll(playlist).map(MatchResult::value).toMutableList()
                            val audioIndex = qualities.indexOfFirst { it.equals("Audio Only", true) }
                            qualities.removeAt(audioIndex)
                            qualities.add(getApplication<Application>().getString(R.string.audio_only))
                            urls.add(urls.removeAt(audioIndex))
                            val map = qualities.zip(urls).toMap()
                            val mediaPlaylist = URL(map.values.elementAt(0)).openStream().use {
                                PlaylistParser(it, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                            }
                            var totalDuration = 0L
                            val size = mediaPlaylist.tracks.size
                            val relativeTimes = ArrayList<Long>(size)
                            val durations = ArrayList<Long>(size)
                            var time = 0L
                            mediaPlaylist.tracks.forEach {
                                val duration = (it.trackInfo.duration * 1000f).toLong()
                                durations.add(duration)
                                totalDuration += duration
                                relativeTimes.add(time)
                                time += duration
                            }
                            VideoDownloadInfo(video, map, relativeTimes, durations, totalDuration, mediaPlaylist.targetDuration * 1000L, 0)
                        } else {
                            throw IllegalAccessException()
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = {
                        setVideoInfo(it)
                    }, onError = {
                        if (it is IllegalAccessException) {
                            val context = getApplication<Application>()
                            Toast.makeText(context, context.getString(R.string.video_subscribers_only), Toast.LENGTH_LONG).show()
                            _videoInfo.value = null
                        }
                    })
                    .addTo(compositeDisposable)
        }
    }

    fun setVideoInfo(videoInfo: VideoDownloadInfo) {
        if (_videoInfo.value != videoInfo) {
            _videoInfo.value = videoInfo
        }
    }

    fun download(url: String, path: String, quality: String, fromIndex: Int, toIndex: Int, wifiOnly: Boolean) {
        GlobalScope.launch {
            with(_videoInfo.value!!) {
                val context = getApplication<Application>()
                val startPosition = relativeStartTimes[fromIndex]
                val endPosition = relativeStartTimes[toIndex]
                val duration = endPosition + durations[toIndex] - startPosition
                val directory = "$path${File.separator}${video.id}${if (!quality.contains("Audio", true)) quality else "audio"}${File.separator}"
                val offlineVideo = DownloadUtils.prepareDownload(context, video, url, directory, duration, startPosition, endPosition)
                val videoId = offlineRepository.saveVideo(offlineVideo)
                DownloadUtils.download(context, VideoRequest(videoId.toInt(), video.id, url, directory, fromIndex, toIndex), wifiOnly)
            }
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}