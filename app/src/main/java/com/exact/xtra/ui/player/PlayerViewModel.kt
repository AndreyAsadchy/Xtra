package com.exact.xtra.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.exact.xtra.R
import com.exact.xtra.ui.Initializable
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
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.disposables.CompositeDisposable

abstract class PlayerViewModel(
        context: Application,
        playerType: PlayerType) : AndroidViewModel(context), Player.EventListener, Initializable {

    val player: SimpleExoPlayer
    protected val dataSourceFactory: DataSource.Factory
    protected val trackSelector: DefaultTrackSelector
    protected val compositeDisposable = CompositeDisposable()
    protected lateinit var mediaSource: MediaSource

    init {
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
        trackSelector = DefaultTrackSelector()
        if (playerType == PlayerType.VIDEO) {
            trackSelector.parameters = trackSelector.buildUponParameters().setForceHighestSupportedBitrate(true).build()
        }
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player.addListener(this)
    }

    fun play() {
        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    override fun onCleared() {
        player.release()
        compositeDisposable.clear()
        super.onCleared()
    }

    override fun isInitialized(): Boolean {
        return ::mediaSource.isInitialized
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