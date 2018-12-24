package com.github.exact7.xtra.ui.player

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.OnQualityChangeListener
import com.github.exact7.xtra.ui.player.PlayerMode.AUDIO_ONLY
import com.github.exact7.xtra.ui.player.PlayerMode.DISABLED
import com.github.exact7.xtra.ui.player.PlayerMode.NORMAL
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.LinkedList
import java.util.regex.Pattern
import kotlin.collections.set

abstract class HlsPlayerViewModel(context: Application) : PlayerViewModel(context), OnQualityChangeListener {

    private companion object {
        const val VIDEO_RENDERER = 0
        const val AUDIO_RENDERER = 1
        const val TAG = "HlsPlayerViewModel"
    }

    private val prefs = context.getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
    private lateinit var tempList: List<CharSequence>
    val helper = PlayerHelper()

    override fun changeQuality(index: Int) {
        helper.selectedQualityIndex = index
        when (index) {
            in 0..helper.qualities.value!!.lastIndex -> {
                val quality = when (index) {
                    0 -> {
                        trackSelector.setParameters(trackSelector.buildUponParameters().clearSelectionOverrides())
                        "Auto"
                    }
                    else -> {
                        updateQuality()
                        helper.qualities.value!![index - 1].toString()
                    }
                }

                changePlayerMode(NORMAL)
                prefs.edit { putString(TAG, quality) }
            }
            helper.qualities.value!!.lastIndex + 1 -> changePlayerMode(AUDIO_ONLY)
            else -> changePlayerMode(DISABLED)
        }
    }

    private var initialized = false //TODO reset in oncleared?

    private fun changePlayerMode(playerMode: PlayerMode) {
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

    protected fun updateQuality() {
        trackSelector.currentMappedTrackInfo?.let {
            val parametersBuilder = trackSelector.buildUponParameters()
                    .setSelectionOverride(VIDEO_RENDERER, it.getTrackGroups(VIDEO_RENDERER), DefaultTrackSelector.SelectionOverride(0, helper.selectedQualityIndex - 1))
            trackSelector.setParameters(parametersBuilder)
        }
    }

    override fun onResume() {
        super.onResume()
        updateQuality()
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (!initialized) {
            trackSelector.currentMappedTrackInfo?.let {
                initialized = true
                val index = prefs.getString(TAG, "Auto").let { quality: String ->
                    if (quality == "Auto") {
                        0
                    } else {
                        val index = tempList.indexOf(quality)
                        if (index != -1) {
                            index + 1
                        } else {
                            0
                        }
                    }
                }
                helper.selectedQualityIndex = index
                if (index != 0) {
                    updateQuality()
                }
                helper.qualities.value = tempList
            }
        }
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        if (helper.qualities.value == null) {
            if (manifest == null) return
            val masterPlaylist = (manifest as HlsManifest).masterPlaylist
            val tags = masterPlaylist.tags
            val urls = LinkedHashMap<CharSequence, String>(tags.size)
            val pattern = Pattern.compile("NAME=\"(.+)\"")
            var trackIndex = 0
            val audioOnly = getApplication<Application>().getString(R.string.audio_only)
            tags.forEach {
                val matcher = pattern.matcher(it)
                if (matcher.find()) {
                    val quality = matcher.group(1)
                    val url = masterPlaylist.variants[trackIndex++].url
                    urls[if (!quality.startsWith("audio", true)) quality else audioOnly] = url
                }
            }
            helper.urls.putAll(urls)
            tempList = LinkedList(urls.keys).apply {
                add(removeAt(indexOf(audioOnly))) //move audio option to bottom
            }
        }
    }
}
