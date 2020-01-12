package com.github.exact7.xtra.ui.player

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.player.lowlatency.HlsManifest
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.exact7.xtra.ui.player.PlayerMode.NORMAL
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.*
import java.util.regex.Pattern

private const val VIDEO_RENDERER = 0
private const val TAG = "HlsPlayerViewModel"

abstract class HlsPlayerViewModel(
        context: Application,
        val repository: TwitchService) : PlayerViewModel(context), FollowViewModel {

    private val prefs = context.getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
    protected val helper = PlayerHelper()
    val loaded: LiveData<Boolean>
        get() = helper.loaded
    lateinit var qualities: List<String>
        private set
    override lateinit var follow: FollowLiveData

    override fun changeQuality(index: Int) {
        qualityIndex = index
    }

    protected fun setVideoQuality(index: Int) {
        val quality = if (index == 0) {
            trackSelector.setParameters(trackSelector.buildUponParameters().clearSelectionOverrides())
            "Auto"
        } else {
            updateVideoQuality()
            qualities[index]
        }
        prefs.edit { putString(TAG, quality) }
        if (_playerMode.value != NORMAL) {
            if (playerMode.value == AUDIO_ONLY) {
                stopBackgroundAudio()
                _currentPlayer.value = player
                play()
            }
            _playerMode.value = NORMAL
        }
    }

    private fun updateVideoQuality() {
        trackSelector.parameters = trackSelector.buildUponParameters()
                .setSelectionOverride(VIDEO_RENDERER, trackSelector.currentMappedTrackInfo!!.getTrackGroups(VIDEO_RENDERER), DefaultTrackSelector.SelectionOverride(0, qualityIndex - 1))
                .build()
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (trackSelector.currentMappedTrackInfo != null) {
            if (helper.loaded.value != true) {
                helper.loaded.value = true
                val index = prefs.getString(TAG, "Auto").let { quality ->
                    if (quality == "Auto") {
                        0
                    } else {
                        qualities.indexOf(quality).let { if (it != -1) it else 0 }
                    }
                }
                qualityIndex = index
            }
            if (qualityIndex != 0) {
                updateVideoQuality()
            }
        }
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        if (helper.urls.isEmpty() && manifest is HlsManifest) {
            manifest.masterPlaylist.let {
                val context = getApplication<Application>()
                val tags = it.tags
                val urls = LinkedHashMap<String, String>(tags.size)
                val audioOnly = context.getString(R.string.audio_only)
                val pattern = Pattern.compile("NAME=\"(.+)\"")
                var trackIndex = 0
                tags.forEach { tag ->
                    val matcher = pattern.matcher(tag)
                    if (matcher.find()) {
                        val quality = matcher.group(1)
                        val url = it.variants[trackIndex++].url.toString()
                        urls[if (!quality.startsWith("audio", true)) quality else audioOnly] = url
                    }
                }
                helper.urls = urls.apply {
                    put(audioOnly, remove(audioOnly)!!) //move audio option to bottom
                }
                qualities = LinkedList(urls.keys).apply {
                    addFirst(context.getString(R.string.auto))
                    if (this@HlsPlayerViewModel is StreamPlayerViewModel) {
                        add(context.getString(R.string.chat_only))
                    }
                }
            }
        }
    }

    override fun setUser(user: LoggedIn) {
        if (!this::follow.isInitialized) { //TODO REFACTOR
            follow = FollowLiveData(repository, user, channelInfo.first)
        }
    }

    override fun onResume() {
        isResumed = true
        if (playerMode.value == NORMAL) {
            super.onResume()
        } else if (playerMode.value == AUDIO_ONLY) {
            hideBackgroundAudio()
        }
    }

    override fun onPause() {
        isResumed = false
        if (playerMode.value == NORMAL) {
            super.onPause()
        } else if (playerMode.value == AUDIO_ONLY) {
            showBackgroundAudio()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (playerMode.value == AUDIO_ONLY && isResumed) {
            stopBackgroundAudio()
        }
    }
}
