package com.github.exact7.xtra.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.github.exact7.xtra.R
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
import com.google.android.exoplayer2.util.Util
import io.reactivex.disposables.CompositeDisposable

abstract class PlayerViewModel(context: Application) : AndroidViewModel(context), Player.EventListener {

    protected val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
    protected val trackSelector = DefaultTrackSelector()
    protected val compositeDisposable = CompositeDisposable()
    val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector).apply { addListener(this@PlayerViewModel) }

    protected fun play(mediaSource: MediaSource) {
        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    override fun onCleared() {
        player.release()
        compositeDisposable.clear()
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

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

    }

    override fun onSeekProcessed() {

    }
}