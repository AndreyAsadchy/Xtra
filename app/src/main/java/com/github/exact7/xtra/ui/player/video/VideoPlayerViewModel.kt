package com.github.exact7.xtra.ui.player.video

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.VideoInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.service.DownloadWorker
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val offlineRepository: OfflineRepository) : HlsPlayerViewModel(context) {

    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var playbackProgress: Long = 0
    private val mediaPlaylist by lazy { (player.currentManifest as HlsManifest).mediaPlaylist } //it gets current playlist and not the one to download
    val videoInfo: VideoInfo
        get() = VideoInfo(helper.qualities.value!!, mediaPlaylist.segments.map { it.relativeStartTimeUs }, toSeconds(mediaPlaylist.durationUs), toSeconds(mediaPlaylist.targetDurationUs), player.currentPosition / 1000)

    fun setVideo(video: Video) {
        if (_video.value != video) {
            _video.value = video
            playerRepository.fetchVideoPlaylist(video.id)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {

                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackProgress)
    }

    override fun onPause() {
        super.onPause()
        playbackProgress = player.currentPosition
    }

    fun download(quality: String, segmentFrom: Int, segmentTo: Int) {
        GlobalScope.launch {
            val id = offlineRepository.saveRequest(VideoRequest(
                    video.value!!,
                    quality,
                    helper.urls[quality]!!.substringBeforeLast('/') + "/",
                    ArrayList(mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { it.url to toSeconds(it.durationUs) }),
                    toSeconds(mediaPlaylist.targetDurationUs).toInt()))
            DownloadWorker.download(id.toInt(), DownloadWorker.TYPE_VIDEO)
        }
    }

    private fun toSeconds(value: Long) = value / 1000000L
}
