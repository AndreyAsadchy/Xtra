package com.github.exact7.xtra.ui.player

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.BaseAndroidViewModel
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.util.isNetworkAvailable
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UseExperimental(ExperimentalCoroutinesApi::class)
abstract class PlayerViewModel(context: Application) : BaseAndroidViewModel(context), Player.EventListener, CoroutineScope by MainScope() {

    protected val tag: String = javaClass.simpleName

    protected val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
    protected val trackSelector = DefaultTrackSelector()
    protected open val loadControl = DefaultLoadControl()
    val player: SimpleExoPlayer by lazy { ExoPlayerFactory.newSimpleInstance(
            context,
            DefaultRenderersFactory(context),
            trackSelector,
            loadControl).apply { addListener(this@PlayerViewModel) }
    }
    protected lateinit var mediaSource: MediaSource //TODO maybe redo these viewmodels to custom players

    protected fun play() {
        if (this::mediaSource.isInitialized) { //TODO
            player.prepare(mediaSource)
            player.playWhenReady = true
        }
    }

    open fun onResume() {
        play()
    }

    open fun onPause() {
        player.stop()
    }

    override fun onCleared() {
        player.release()
        cancel()
        super.onCleared()
    }

    //Player.EventListener

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Log.e(tag, "Player error", error)
        val context = getApplication<Application>()
        if (context.isNetworkAvailable) {
            try {
                val isStreamEnded = try {
                    error.type == ExoPlaybackException.TYPE_SOURCE &&
                            this@PlayerViewModel is StreamPlayerViewModel &&
                            error.sourceException.let { it is HttpDataSource.InvalidResponseCodeException && it.responseCode == 404 }
                } catch (e: IllegalStateException) {
                    Crashlytics.log(Log.ERROR, tag, "onPlayerError: Stream end check error. Type: ${error.type}")
                    Crashlytics.logException(e)
                    return
                }
                if (isStreamEnded) {
                    Toast.makeText(context, context.getString(R.string.stream_ended), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.player_error), Toast.LENGTH_SHORT).show()
                    launch {
                        withContext(Dispatchers.Default) {
                            delay(1500L)
                        }
                        try {
                            if (this@PlayerViewModel is StreamPlayerViewModel) {
                                play()
                            } else {
                                onResume()
                            }
                        } catch (e: Exception) {
                            Crashlytics.log(Log.ERROR, tag, "onPlayerError: Retry error. ${e.message}")
                            Crashlytics.logException(e)
                        }
                    }
                }
            } catch (e: Exception) {
                Crashlytics.log(Log.ERROR, tag, "onPlayerError ${e.message}")
                Crashlytics.logException(e)
            }
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

    }

    override fun onSeekProcessed() {

    }
}