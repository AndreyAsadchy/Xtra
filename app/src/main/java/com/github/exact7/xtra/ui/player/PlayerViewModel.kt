package com.github.exact7.xtra.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.BaseAndroidViewModel
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.util.isNetworkAvailable
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
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
    val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            context,
            DefaultRenderersFactory(context),
            trackSelector).apply { addListener(this@PlayerViewModel) }
    protected lateinit var mediaSource: MediaSource //TODO maybe redo these viewmodels to custom players

    protected val _currentPlayer = MutableLiveData<ExoPlayer>().apply { value = player }
    val currentPlayer: LiveData<ExoPlayer>
        get() = _currentPlayer
    protected val _playerMode = MutableLiveData<PlayerMode>().apply { value = PlayerMode.NORMAL }
    val playerMode: LiveData<PlayerMode>
        get() = _playerMode
    var qualityIndex = 0
        protected set

    protected var binder: AudioPlayerService.AudioBinder? = null
    private val connection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as AudioPlayerService.AudioBinder
            _currentPlayer.value = service.player
        }
    }

    protected var isResumed = true

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

    protected fun startBackgroundAudio(playlistUrl: String, channelName: String, title: String, imageUrl: String, usePlayPause: Boolean) {
        player.stop()
        val context = getApplication<Application>()
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            putExtra(AudioPlayerService.KEY_PLAYLIST_URL, playlistUrl)
            putExtra(AudioPlayerService.KEY_CHANNEL_NAME, channelName)
            putExtra(AudioPlayerService.KEY_TITLE, title)
            putExtra(AudioPlayerService.KEY_IMAGE_URL, imageUrl)
            putExtra(AudioPlayerService.KEY_USE_PLAY_PAUSE, usePlayPause)
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    protected fun stopBackgroundAudio() {
        val context = getApplication<Application>()
        context.unbindService(connection)
    }

    protected fun showBackgroundAudio() {
        binder?.showNotification()
    }

    protected fun hideBackgroundAudio() {
        binder?.hideNotification()
    }

    override fun onCleared() {
        player.release()
        cancel()
        super.onCleared()
    }

    //Player.EventListener

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
}