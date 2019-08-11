package com.github.exact7.xtra.ui.player.clip

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.clip.Clip
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
        super.onPause()
        playbackPosition = player.currentPosition
    }

    fun setClip(clip: Clip) {
        if (!this::clip.isInitialized) {
            this.clip = clip
            playerRepository.loadClipQualities(clip.slug)
                    .subscribe({
                        helper.urls = it
                        val quality = prefs.getString(TAG, it.keys.first())!!
                        play((it[quality] ?: it.values.first()))
                        qualityIndex = it.keys.indexOf(quality)
                        helper.loaded.value = true
                    }, {

                    })
                    .addTo(compositeDisposable)
        }
    }

    private fun play(url: String) {
        mediaSource = factory.createMediaSource(url.toUri())
        play()
        player.seekTo(playbackPosition)
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
}
