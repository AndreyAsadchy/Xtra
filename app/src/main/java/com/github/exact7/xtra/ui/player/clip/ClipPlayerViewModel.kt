package com.github.exact7.xtra.ui.player.clip

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.player.PlayerHelper
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

private const val TAG = "ClipPlayerViewModel"

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val repository: TwitchService) : PlayerViewModel(context), FollowViewModel {

    private lateinit var clip: Clip
    private val factory: ProgressiveMediaSource.Factory = ProgressiveMediaSource.Factory(dataSourceFactory)
    private val prefs = context.getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
    private val helper = PlayerHelper()
    val qualities: Map<String, String>
        get() = helper.urls
    val loaded: LiveData<Boolean>
        get() = helper.loaded
    override val channelInfo: Pair<String, String>
        get() = clip.broadcaster.id to clip.broadcaster.displayName
    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var loadingVideo = false

    override lateinit var follow: FollowLiveData

    override fun changeQuality(index: Int) {
        playbackPosition = player.currentPosition
        val quality = helper.urls.values.elementAt(index)
        play(quality)
        prefs.edit { putString(TAG, helper.urls.keys.elementAt(index)) }
        qualityIndex = index
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackPosition)
    }

    override fun onPause() {
        playbackPosition = player.currentPosition
        super.onPause()
    }

    fun setClip(clip: Clip) {
        if (!this::clip.isInitialized) {
            this.clip = clip
            playerRepository.loadClipUrls(clip.slug)
                    .subscribe({ urls ->
                        val preferredQuality = prefs.getString(TAG, null)
                        if (preferredQuality != null) {
                            var url: String? = null
                            for (entry in urls.entries.withIndex()) {
                                if (entry.value.key == preferredQuality) {
                                    url = entry.value.value
                                    qualityIndex = entry.index
                                    break
                                }
                            }
                            url.let {
                                if (it != null) {
                                    play(it)
                                } else {
                                    play(urls.keys.first())
                                }
                            }
                        } else {
                            play(urls.keys.first())
                        }
                        helper.urls = urls
                        helper.loaded.value = true
                    }, {

                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val context = getApplication<Application>()
        Toast.makeText(context, context.getString(R.string.player_error), Toast.LENGTH_SHORT).show()
        changeQuality(++qualityIndex)
        Crashlytics.logException(error)
    }

    fun loadVideo() {
        if (!loadingVideo) {
            loadingVideo = true
            repository.loadVideo(clip.vod!!.id)
                    .doOnEvent { _, _ -> loadingVideo = false }
                    .subscribeBy(onSuccess = {
                        _video.value = it
                    })
                    .addTo(compositeDisposable)

        }
    }

    private fun play(url: String) {
        mediaSource = factory.createMediaSource(url.toUri())
        play()
        player.seekTo(playbackPosition)
    }

}
