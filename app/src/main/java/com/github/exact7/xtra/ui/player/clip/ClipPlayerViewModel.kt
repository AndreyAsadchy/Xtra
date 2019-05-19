package com.github.exact7.xtra.ui.player.clip

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.OnQualityChangeListener
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.player.PlayerHelper
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.source.ExtractorMediaSource
import javax.inject.Inject

private const val TAG = "ClipPlayerViewModel"

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val repository: TwitchService) : PlayerViewModel(context), OnQualityChangeListener, FollowViewModel {

    private val _clip = MutableLiveData<Clip>()
    val clip: LiveData<Clip>
        get() = _clip
    private val factory: ExtractorMediaSource.Factory = ExtractorMediaSource.Factory(dataSourceFactory)
    private var playbackProgress: Long = 0
    private val prefs = context.getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
    private val helper = PlayerHelper()
    val qualities: Map<String, String>
        get() = helper.urls!!
    val loaded: LiveData<Boolean>
        get() = helper.loaded
    val selectedQualityIndex: Int
        get() = helper.selectedQualityIndex
    override val channelInfo: Pair<String, String>
        get() {
            val c = clip.value!!
            return c.broadcaster.id to c.broadcaster.displayName
        }
    override lateinit var follow: FollowLiveData

    override fun changeQuality(index: Int) {
        playbackProgress = player.currentPosition
        val quality = helper.urls!!.values.elementAt(index)
        play(quality)
        prefs.edit { putString(TAG, quality) }
        helper.selectedQualityIndex = index
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackProgress)
    }

    override fun onPause() {
        super.onPause()
        playbackProgress = player.currentPosition
    }

    fun setClip(clip: Clip) {
        if (_clip.value != clip) {
            _clip.value = clip
            call(playerRepository.loadClipQualities(clip.slug)
                    .subscribe({
                        helper.urls = it
                        play(it.values.first())
                        helper.selectedQualityIndex = 0
                        helper.loaded.value = true
                    }, {

                    }))
            clip.vod?.let {
                initChat(playerRepository, clip.broadcaster.id, clip.broadcaster.name)
            }
        }
    }

    private fun play(url: String) {
        mediaSource = factory.createMediaSource(url.toUri())
        play()
        player.seekTo(playbackProgress)
    }

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        playbackProgress = player.currentPosition
    }

    override fun onCleared() {
//        chatReplayManager?.stop()
        super.onCleared()
    }
}
