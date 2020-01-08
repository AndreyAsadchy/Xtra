package com.github.exact7.xtra.ui.player.video

import android.app.Application
import android.net.Uri
import android.widget.Toast
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.VideoDownloadInfo
import com.github.exact7.xtra.model.VideoPosition
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.player.lowlatency.HlsManifest
import com.github.exact7.xtra.player.lowlatency.HlsMediaSource
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.AudioPlayerService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository) {

    private lateinit var video: Video
    val videoInfo: VideoDownloadInfo?
        get() {
            val playlist = (player.currentManifest as? HlsManifest)?.mediaPlaylist ?: return null
            val segments = playlist.segments
            val size = segments.size
            val relativeTimes = ArrayList<Long>(size)
            val durations = ArrayList<Long>(size)
            for (i in 0 until size) {
                val segment = segments[i]
                relativeTimes.add(segment.relativeStartTimeUs / 1000L)
                durations.add(segment.durationUs / 1000L)
            }
            return VideoDownloadInfo(video, helper.urls, relativeTimes, durations, playlist.durationUs / 1000L, playlist.targetDurationUs / 1000L, player.currentPosition)
        }

    override val channelInfo: Pair<String, String>
        get() = video.channel.id to video.channel.displayName

    fun setVideo(video: Video, offset: Double) {
        if (!this::video.isInitialized) {
            this.video = video
            playerRepository.loadVideoPlaylist(video.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = {
                        if (it.isSuccessful) {
                            mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(it.raw().request().url().toString()))
                            play()
                            if (offset > 0) {
                                player.seekTo(offset.toLong())
                            }
                        } else if (it.code() == 403) {
                            val context = getApplication<Application>()
                            Toast.makeText(context, context.getString(R.string.video_subscribers_only), Toast.LENGTH_LONG).show()
                        }
                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun changeQuality(index: Int) {
        previousQuality = qualityIndex
        super.changeQuality(index)
        when {
            index < qualities.lastIndex -> {
                val audioOnly = playerMode.value == PlayerMode.AUDIO_ONLY
                if (audioOnly) {
                    playbackPosition = currentPlayer.value!!.currentPosition
                }
                setVideoQuality(index)
                if (audioOnly) {
                    player.seekTo(playbackPosition)
                }
            }
            else -> {
                (player.currentManifest as? HlsManifest)?.let {
                    startBackgroundAudio(helper.urls.values.last(), video.channel.status, video.channel.displayName, video.channel.logo, true, AudioPlayerService.TYPE_VIDEO, video.id.substring(1).toLong())
                    _playerMode.value = PlayerMode.AUDIO_ONLY
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerMode.value != PlayerMode.AUDIO_ONLY) {
            player.seekTo(playbackPosition)
        }
    }

    override fun onPause() {
        if (playerMode.value != PlayerMode.AUDIO_ONLY) {
            playbackPosition = player.currentPosition
        }
        super.onPause()
    }

    override fun onCleared() {
        if (playerMode.value == PlayerMode.NORMAL) {
            playerRepository.saveVideoPosition(VideoPosition(video.id.substring(1).toLong(), player.currentPosition))
        }
        super.onCleared()
    }
}