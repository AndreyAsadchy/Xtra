package com.github.andreyasadchy.xtra.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.ui.common.BaseAndroidViewModel
import com.github.andreyasadchy.xtra.ui.common.OnQualityChangeListener
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.isNetworkAvailable
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

private const val SPEED_TAG = "PlayerViewModel_Speed"

abstract class PlayerViewModel(context: Application) : BaseAndroidViewModel(context), Player.EventListener, OnQualityChangeListener {

    protected val tag: String = javaClass.simpleName

    protected val httpDataSourceFactory = DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.getString(R.string.app_name)))
    protected val dataSourceFactory = DefaultDataSourceFactory(context, null, httpDataSourceFactory)



    protected val trackSelector = DefaultTrackSelector()
    val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            context,
            trackSelector,
            DefaultLoadControl.Builder()
                    .setBufferDurationsMs(15000, 50000, 2000, 5000)
                    .createDefaultLoadControl())
            .apply { addListener(this@PlayerViewModel) }
    protected lateinit var mediaSource: MediaSource //TODO maybe redo these viewmodels to custom players

    protected val _currentPlayer = MutableLiveData<ExoPlayer>().apply { value = player }
    val currentPlayer: LiveData<ExoPlayer>
        get() = _currentPlayer
    protected val _playerMode = MutableLiveData<PlayerMode>().apply { value = PlayerMode.NORMAL }
    val playerMode: LiveData<PlayerMode>
        get() = _playerMode
    var qualityIndex = 0
        protected set
    protected var previousQuality = 0
    protected var playbackPosition: Long = 0

    protected var binder: AudioPlayerService.AudioBinder? = null

    protected var isResumed = true

    private var timer: Timer? = null
    private val _sleepTimer = MutableLiveData<Boolean>()
    val sleepTimer: LiveData<Boolean>
        get() = _sleepTimer
    private var timerEndTime = 0L
    val timerTimeLeft
        get() = timerEndTime - System.currentTimeMillis()

    protected val userPrefs: SharedPreferences = context.getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)

    init {
        val speed = userPrefs.getFloat(SPEED_TAG, 1f)
        if (speed != 1f) {
            setSpeed(speed, false)
        }
    }

    fun setTimer(duration: Long) {
        timer?.let {
            it.cancel()
            timerEndTime = 0L
            timer = null
        }
        if (duration > 0L) {
            timer = Timer().apply {
                timerEndTime = System.currentTimeMillis() + duration
                schedule(duration) {
                    player.release()
                    stopBackgroundAudio()
                    _sleepTimer.postValue(true)
                }
            }
        }
    }

    open fun onResume() {
        play()
    }

    open fun onPause() {
        player.stop()
    }

    open fun restartPlayer() {
        playbackPosition = currentPlayer.value!!.currentPosition
        player.stop()
        play()
        player.seekTo(playbackPosition)
    }

    protected fun play() {
        if (this::mediaSource.isInitialized) { //TODO
            player.prepare(mediaSource)
            player.playWhenReady = true
        }
    }

    protected fun startBackgroundAudio(playlistUrl: String, channelName: String, title: String?, imageUrl: String, usePlayPause: Boolean, type: Int, videoId: Number?) {
        val context = XtraApp.INSTANCE //TODO
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            putExtra(AudioPlayerService.KEY_PLAYLIST_URL, playlistUrl)
            putExtra(AudioPlayerService.KEY_CHANNEL_NAME, channelName)
            putExtra(AudioPlayerService.KEY_TITLE, title)
            putExtra(AudioPlayerService.KEY_IMAGE_URL, imageUrl)
            putExtra(AudioPlayerService.KEY_USE_PLAY_PAUSE, usePlayPause)
            putExtra(AudioPlayerService.KEY_CURRENT_POSITION, player.currentPosition)
            putExtra(AudioPlayerService.KEY_TYPE, type)
            putExtra(AudioPlayerService.KEY_VIDEO_ID, videoId)
        }
        player.stop()
        val connection = object : ServiceConnection {

            override fun onServiceDisconnected(name: ComponentName) {
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = service as AudioPlayerService.AudioBinder
                _currentPlayer.value = service.player
            }
        }
        AudioPlayerService.connection = connection
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    protected fun stopBackgroundAudio() {
        AudioPlayerService.connection?.let {
//            val context = getApplication<Application>()
            XtraApp.INSTANCE.unbindService(it) //TODO
        }
    }

    protected fun showBackgroundAudio() {
        binder?.showNotification()
    }

    protected fun hideBackgroundAudio() {
        if (AudioPlayerService.connection != null) {
            binder?.hideNotification()
        } else {
            qualityIndex = previousQuality
            _currentPlayer.value = player
            play()
            player.seekTo(AudioPlayerService.position)
        }
    }

    //Player.EventListener

    override fun onPlayerError(error: ExoPlaybackException) {
        Log.e(tag, "Player error", error)
        playbackPosition = player.currentPosition
        val context = getApplication<Application>()
        if (context.isNetworkAvailable) {
            try {
                val isStreamEnded = try {
                    error.type == ExoPlaybackException.TYPE_SOURCE &&
                            this@PlayerViewModel is StreamPlayerViewModel &&
                            error.sourceException.let { it is HttpDataSource.InvalidResponseCodeException && it.responseCode == 404 }
                } catch (e: IllegalStateException) {
//                    Crashlytics.log(Log.ERROR, tag, "onPlayerError: Stream end check error. Type: ${error.type}")
//                    Crashlytics.logException(e)
                    return
                }
                if (isStreamEnded) {
                    context.toast(R.string.stream_ended)
                } else {
                    context.shortToast(R.string.player_error)
                    viewModelScope.launch {
                        delay(1500L)
                        try {
                            restartPlayer()
                        } catch (e: Exception) {
//                            Crashlytics.log(Log.ERROR, tag, "onPlayerError: Retry error. ${e.message}")
//                            Crashlytics.logException(e)
                        }
                    }
                }
            } catch (e: Exception) {
//                Crashlytics.log(Log.ERROR, tag, "onPlayerError ${e.message}")
//                Crashlytics.logException(e)
            }
            FirebaseCrashlytics.getInstance().recordException(error)
        }
    }

    override fun onCleared() {
        player.release()
        timer?.cancel()
    }

    open fun setSpeed(speed: Float, save: Boolean = true) {
        player.playbackParameters = PlaybackParameters(speed)
        if (save) userPrefs.edit { putFloat(SPEED_TAG, speed) }
    }
}