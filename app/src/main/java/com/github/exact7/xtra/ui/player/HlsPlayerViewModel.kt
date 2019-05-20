package com.github.exact7.xtra.ui.player

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.OnQualityChangeListener
import com.github.exact7.xtra.ui.common.follow.FollowLiveData
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.exact7.xtra.ui.player.PlayerMode.DISABLED
import com.github.exact7.xtra.ui.player.PlayerMode.NORMAL
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.regex.Pattern

private const val VIDEO_RENDERER = 0
private const val AUDIO_RENDERER = 1
private const val TAG = "HlsPlayerViewModel"

abstract class HlsPlayerViewModel(
        context: Application,
        val repository: TwitchService) : PlayerViewModel(context), OnQualityChangeListener, FollowViewModel {

    private val prefs = context.getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
    protected val helper = PlayerHelper()
    val loaded: LiveData<Boolean>
        get() = helper.loaded
    val selectedQualityIndex: Int
        get() = helper.selectedQualityIndex
    lateinit var qualities: List<String>
        private set
    override lateinit var follow: FollowLiveData

    override fun changeQuality(index: Int) {
        helper.selectedQualityIndex = index
    }

    protected fun updateQuality(index: Int) {
        val quality = if (index == 0) {
            trackSelector.setParameters(trackSelector.buildUponParameters().clearSelectionOverrides())
            "Auto"
        } else {
            updateQuality()
            qualities[index]
        }
        changePlayerMode(NORMAL)
        prefs.edit { putString(TAG, quality) }
    }

    private fun updateQuality() {
        val parametersBuilder = trackSelector.buildUponParameters()
                .setSelectionOverride(VIDEO_RENDERER, trackSelector.currentMappedTrackInfo?.getTrackGroups(VIDEO_RENDERER), DefaultTrackSelector.SelectionOverride(0, helper.selectedQualityIndex - 1))
        trackSelector.setParameters(parametersBuilder)
    }

    protected fun changePlayerMode(playerMode: PlayerMode) {
        val videoDisabled: Boolean
        val audioDisabled: Boolean
        when (playerMode) {
            NORMAL -> {
                videoDisabled = false
                audioDisabled = false
            }
            AUDIO_ONLY -> {
                videoDisabled = true
                audioDisabled = false
            }
            DISABLED -> {
                videoDisabled = true
                audioDisabled = true
            }
        }
        trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(VIDEO_RENDERER, videoDisabled).setRendererDisabled(AUDIO_RENDERER, audioDisabled))
    }

    override fun onResume() {
        super.onResume()
        updateQuality()
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) { //TODO fix incorrect quality after resuming
        if (helper.loaded.value != true && trackSelector.currentMappedTrackInfo != null) {
            helper.loaded.value = true
            val index =  prefs.getString(TAG, "Auto").let { quality: String ->
                if (quality == "Auto") {
                    0
                } else {
                    qualities.indexOf(quality).let { if (it != -1) it else 0 }
                }
            }
            helper.selectedQualityIndex = index
            if (index != 0) {
                updateQuality()
            }
        }
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        if (helper.urls == null && manifest is HlsManifest) {
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
                        val url = it.variants[trackIndex++].url
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
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(repository, user, channelInfo.first)
        }
    }
}
