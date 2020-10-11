package com.github.exact7.xtra.ui.player.clip

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.GraphQLRepositoy
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.player.PlayerHelper
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.shortToast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClipPlayerViewModel"

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val graphQLRepositoy: GraphQLRepositoy,
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
            viewModelScope.launch {
                try {
                    val urls = graphQLRepositoy.loadClipUrls(clip.slug)
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
                                play(urls.values.first())
                            }
                        }
                    } else {
                        play(urls.values.first())
                    }
                    helper.urls = urls
                    helper.loaded.value = true
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first, viewModelScope)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (error.type == ExoPlaybackException.TYPE_UNEXPECTED && error.unexpectedException is IllegalStateException) {
            val context = getApplication<Application>()
            context.shortToast(R.string.player_error)
            if (qualityIndex < helper.urls.size - 1) {
                changeQuality(++qualityIndex)
            }
        }
    }

    fun loadVideo() {
        if (!loadingVideo) {
            loadingVideo = true
            viewModelScope.launch {
                try {
                    val video = repository.loadVideo(clip.vod!!.id)
                    _video.postValue(video)
                } catch (e: Exception) {

                } finally {
                    loadingVideo = false
                }
            }
        }
    }

    private fun play(url: String) {
        mediaSource = factory.createMediaSource(url.toUri())
        play()
        player.seekTo(playbackPosition)
    }
}
