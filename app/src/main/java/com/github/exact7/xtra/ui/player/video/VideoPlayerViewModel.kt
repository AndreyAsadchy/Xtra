package com.github.exact7.xtra.ui.player.video

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.VideoDownloadInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.ChatReplayManager
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        repository: TwitchService) : HlsPlayerViewModel(context, repository) {

    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var playbackProgress: Long = 0
    private lateinit var playlist: HlsMediaPlaylist
    val videoInfo: VideoDownloadInfo
        get() {
            val segments = playlist.segments
            val size = segments.size
            val relativeTimes = ArrayList<Long>(size)
            val durations = ArrayList<Long>(size)
            for (i in 0 until size) {
                val segment = segments[i]
                relativeTimes.add(segment.relativeStartTimeUs / 1000L)
                durations.add(segment.durationUs / 1000L)
            }
            return VideoDownloadInfo(_video.value!!, helper.urls!!, relativeTimes, durations, playlist.durationUs / 1000L, playlist.targetDurationUs / 1000L, player.currentPosition)
        }
    override val channelInfo: Pair<String, String>
        get()  {
            val v = video.value!!
            return v.channel.id to v.channel.displayName
        }
    private lateinit var chatReplayManager: ChatReplayManager

    fun setVideo(video: Video) {
        if (_video.value != video) {
            _video.value = video
            call(playerRepository.loadVideoPlaylist(video.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = {
                        if (it.isSuccessful) {
                            mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(it.raw().request().url().toString()))
                            play()
                        } else if (it.code() == 403) {
                            val context = getApplication<Application>()
                            Toast.makeText(context, context.getString(R.string.video_subscribers_only), Toast.LENGTH_LONG).show()
                        }
                    }))
            chatReplayManager = ChatReplayManager(repository, video.id, 0.0, player, this::onMessage, this::clearMessages)
            initChat(playerRepository, video.channel.id, video.channel.name)
        }
    }

    override fun changeQuality(index: Int) {
        super.changeQuality(index)
        when {
            index < qualities.lastIndex -> updateQuality(index)
            else -> changePlayerMode(PlayerMode.AUDIO_ONLY)
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

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        super.onTimelineChanged(timeline, manifest, reason)
        if (!this::playlist.isInitialized && manifest is HlsManifest) {
            playlist = manifest.mediaPlaylist
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        playbackProgress = player.currentPosition
    }

    override fun onCleared() {
        chatReplayManager.stop()
        super.onCleared()
    }
}
