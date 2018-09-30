package com.exact.twitch.ui.player

import android.app.Application
import android.content.Context.MODE_PRIVATE
import com.exact.twitch.ui.Initializable
import com.exact.twitch.ui.OnQualityChangeListener
import com.exact.twitch.ui.player.PlayerMode.AUDIO_ONLY
import com.exact.twitch.ui.player.PlayerMode.DISABLED
import com.exact.twitch.ui.player.PlayerMode.NORMAL
import com.exact.twitch.util.C
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import io.reactivex.disposables.CompositeDisposable
import java.util.LinkedList
import java.util.regex.Pattern

abstract class HlsPlayerViewModel(context: Application) : PlayerViewModel(context, PlayerType.HLS), OnQualityChangeListener {

    companion object {
        const val VIDEO_RENDERER = 0
        const val AUDIO_RENDERER = 1
    }

    val helper = PlayerHelper()

    override fun changeQuality(index: Int, tag: String) {
        when (index) {
            helper.selectedQualityIndex -> return
            in 0 until helper.qualities.value!!.lastIndex -> {
                val parametersBuilder = trackSelector.buildUponParameters()
                when (index) {
                    0 -> parametersBuilder.clearSelectionOverrides() //Auto
                    else -> parametersBuilder.setSelectionOverride(VIDEO_RENDERER, trackSelector.currentMappedTrackInfo?.getTrackGroups(VIDEO_RENDERER), DefaultTrackSelector.SelectionOverride(0, index))
                }
                trackSelector.setParameters(parametersBuilder)
                helper.selectedQualityIndex = index
                changePlayerMode(NORMAL)
                getApplication<Application>().getSharedPreferences(C.USER_PREFS, MODE_PRIVATE).edit().putInt(tag, index).apply()
            }
            helper.qualities.value!!.lastIndex -> changePlayerMode(AUDIO_ONLY)
            else -> changePlayerMode(DISABLED)
        }
    }

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

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        if (helper.qualities.value == null) {
            val masterPlaylist = (manifest as HlsManifest).masterPlaylist
            val tags = masterPlaylist.tags
            val urls = LinkedHashMap<CharSequence, String>(tags.size)
            val pattern = Pattern.compile("NAME=\"(.+)\"")
            var trackIndex = 0
            tags.forEach {
                val matcher = pattern.matcher(it)
                if (matcher.find()) {
                    val quality = matcher.group(1)
                    val url = masterPlaylist.variants[trackIndex++].url
                    urls[if (!quality.startsWith("audio", true)) quality else "Audio only"] = url
                }
            }
            helper.urls.putAll(urls)
            val list = LinkedList(urls.keys)
            list.add(list.removeAt(list.indexOf("Audio only"))) //move audio option to bottom
            helper.qualities.postValue(list)
        }
    }
}
