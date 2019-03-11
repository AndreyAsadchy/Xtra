package com.github.exact7.xtra.ui.download

import android.app.Application
import android.os.Environment
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

    private val _videoInfo = MutableLiveData<VideoDownloadInfo>()
    val videoInfo: LiveData<VideoDownloadInfo>
        get() = _videoInfo

    private val compositeDisposable = CompositeDisposable()

    fun setVideo(video: Video) {
        playerRepository.fetchVideoPlaylist(video.id)
                .map { response ->
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
                    val relativeTimes = mutableListOf<Long>()
                    var time = 0L
                    mediaPlaylist.tracks.forEach {
                        val duration = it.trackInfo.duration.toLong()
                        totalDuration += duration
                        relativeTimes.add(time)
                        time += duration
                    }
                    VideoDownloadInfo(video, map, relativeTimes, totalDuration, mediaPlaylist.targetDuration.toLong(), 0)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    setVideoInfo(it)
                }, {

                })
                .addTo(compositeDisposable)
    }

    fun setVideoInfo(videoInfo: VideoDownloadInfo) {
        _videoInfo.value = videoInfo
    }

    fun download(url: String, quality: String, fromIndex: Int, toIndex: Int, wifiOnly: Boolean, toInternalStorage: Boolean) {
        GlobalScope.launch {
            with(_videoInfo.value!!) {
                val context = getApplication<Application>()
                val duration = relativeStartTimes[toIndex] - relativeStartTimes[fromIndex]
                val rootDirectoryName = if (toInternalStorage) ".downloads" else ".xtra"
                val directory = "$rootDirectoryName${File.separator}${video.id}$quality"
                val path = if (toInternalStorage) {
                    context.getExternalFilesDir(directory)
                } else {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), directory)
                }.let { it!!.absolutePath + File.separator }
                val offlineVideo = DownloadUtils.prepareDownload(context, video, path, duration)
                val videoId = offlineRepository.saveVideo(offlineVideo)
                DownloadUtils.download(context, VideoRequest(videoId.toInt(), video.id, url, path, fromIndex, toIndex), wifiOnly)
            }
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}