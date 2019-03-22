package com.github.exact7.xtra.ui.player.video

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.VideoDownloadInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.github.exact7.xtra.ui.player.PlayerMode
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val repository: TwitchService) : HlsPlayerViewModel(context) {

    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var playbackProgress: Long = 0
    val videoInfo: VideoDownloadInfo
        get() {
            val playlist = (player.currentManifest as HlsManifest).mediaPlaylist
            return VideoDownloadInfo(_video.value!!, helper.urls!!, playlist.segments.map { it.relativeStartTimeUs / 1000000L }, playlist.durationUs / 1000000L, playlist.targetDurationUs / 1000000L, player.currentPosition / 1000)
        }
//    private var chatLogDisposable: Disposable? = null
//    private var chatLogCursor: String? = null

    fun setVideo(video: Video) {
        if (_video.value != video) {
            _video.value = video
            playerRepository.fetchVideoPlaylist(video.id)
                    .map { Uri.parse(it.raw().request().url().toString()) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {

                    })
                    .addTo(compositeDisposable)
//            chatLogDisposable = repository.loadVideoChatLog(video.id, 0.0)
//                    .subscribe({
//                        chatLogCursor = it.next
//                        val handler = Handler()
//                        val list = LinkedList(it.messages)
//                        val runnable = object : Runnable { //TODO maybe there is a kotlin timer function
//                            override fun run() {
//                                var message = list.poll()
//                                while (message != null && message.contentOffsetSeconds <= player.currentPosition / 1000L) {
//                                    println("${message.displayName} : ${message.message}")
//                                    message = list.poll()
//                                }
//                                if (list.size > 10) {
//                                    handler.postDelayed(this, 250)
//                                } else {
//                                    println("FETCH MORE")
//                                }
//                            }
//                        }
//                        handler.post(runnable)
//                    }, {
//
//                    })
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
}
