package com.github.andreyasadchy.xtra.ui.player

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.player.lowlatency.HlsManifest
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.andreyasadchy.xtra.ui.player.PlayerMode.NORMAL
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
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

    private val userPrefs = context.getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
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
        userPrefs.edit { putString(TAG, quality) }
        val mode = _playerMode.value
        if (mode != NORMAL) {
            _playerMode.value = NORMAL
            if (mode == AUDIO_ONLY) {
                stopBackgroundAudio()
                restartPlayer()
                _currentPlayer.value = player
            } else {
                restartPlayer()
            }
        }
    }

    private fun updateVideoQuality() {
        trackSelector.currentMappedTrackInfo?.let { //TODO
            trackSelector.parameters = trackSelector.buildUponParameters()
                    .setSelectionOverride(VIDEO_RENDERER, it.getTrackGroups(VIDEO_RENDERER), DefaultTrackSelector.SelectionOverride(0, qualityIndex - 1))
                    .build()
        }
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (trackSelector.currentMappedTrackInfo != null) {
            if (helper.loaded.value != true) {
                helper.loaded.value = true
                val index = userPrefs.getString(TAG, "Auto").let { quality ->
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
                        val quality = matcher.group(1)!!
                        val url = it.variants[trackIndex++].url.toString()
                        urls[if (!quality.startsWith("audio", true)) quality else audioOnly] = url
                    }
                }
                helper.urls = urls.apply {
                    remove(audioOnly)?.let { url ->
                        put(audioOnly, url) //move audio option to bottom
                    }
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
            follow = FollowLiveData(repository, user, channelInfo.first, viewModelScope)
        }
    }

    override fun onPause() {
        isResumed = false
        if (playerMode.value == NORMAL) {
            helper.loaded.value = false
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
