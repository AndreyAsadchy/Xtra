package com.exact.xtra.ui.player.video

import android.app.Application
import android.content.Intent
import com.exact.xtra.model.VideoInfo
import com.exact.xtra.model.video.Video
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.service.VideoDownloadService
import com.exact.xtra.ui.player.HlsPlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import io.reactivex.rxkotlin.addTo
import kotlinx.android.parcel.RawValue
import java.io.Serializable
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository) : HlsPlayerViewModel(context) {

    lateinit var video: Video
    private var playbackProgress: Long = 0
    private val mediaPlaylist by lazy { (player.currentManifest as HlsManifest).mediaPlaylist }
    val videoInfo: VideoInfo
        get() = VideoInfo(helper.qualities.value!!, mediaPlaylist.segments.map { it.relativeStartTimeUs }, toSeconds(mediaPlaylist.durationUs), toSeconds(mediaPlaylist.targetDurationUs), player.currentPosition / 1000)

    fun init() {
        playerRepository.fetchVideoPlaylist(video.id)
                .subscribe({
                    mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                    startPlayer()
                }, {

                })
                .addTo(compositeDisposable)
    }

    override fun startPlayer() {
        super.startPlayer()
        player.seekTo(playbackProgress)
    }

    override fun changeQuality(index: Int, tag: String) {
        super.changeQuality(index, tag)
        playbackProgress = player.currentPosition
    }

    fun download(quality: String, segmentFrom: Int, segmentTo: Int) {
        val context = getApplication<Application>()
        Intent(context, VideoDownloadService::class.java).apply {
            putExtra("video", video)
            putExtra("quality", quality)
            putExtra("url", helper.urls[quality]!!.substringBeforeLast('/') + "/")
            putExtra("segments", ArrayList(mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { it.url to toSeconds(it.durationUs) }))
            putExtra("target", toSeconds(mediaPlaylist.targetDurationUs).toInt())
            context.startService(this)
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }

    private fun toSeconds(value: Long) = value / 1000000L
}
