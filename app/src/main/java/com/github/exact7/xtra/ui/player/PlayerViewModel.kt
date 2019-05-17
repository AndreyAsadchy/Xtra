package com.github.exact7.xtra.ui.player

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.common.BaseAndroidViewModel
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import com.github.exact7.xtra.util.isNetworkAvailable
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
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
abstract class PlayerViewModel(context: Application) : BaseAndroidViewModel(context), Player.EventListener, OnChatMessageReceivedListener, CoroutineScope by MainScope() {

    protected val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
    protected val trackSelector = DefaultTrackSelector()
    val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector).apply { addListener(this@PlayerViewModel) }
    protected lateinit var mediaSource: MediaSource //TODO maybe redo these viewmodels to custom players

    private val _bttv = MutableLiveData<List<BttvEmote>>()
    val bttv: LiveData<List<BttvEmote>>
        get() = _bttv
    private val _ffz = MutableLiveData<List<FfzEmote>>()
    val ffz: LiveData<List<FfzEmote>>
        get() = _ffz
    protected var subscriberBadges: SubscriberBadgesResponse? = null
    private val _chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList() }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage


    override fun onMessage(message: ChatMessage) {
        message.badges?.find { it.id == "subscriber" }?.let {
            message.subscriberBadge = subscriberBadges?.getBadge(it.version.toInt())
        }
        _chatMessages.value?.add(message)
        _newMessage.postValue(message)
    }

    protected fun clearMessages() {
        _chatMessages.postValue(ArrayList())
    }

    protected fun play() {
        if (this::mediaSource.isInitialized) { //TODO
            player.prepare(mediaSource)
            player.playWhenReady = true
        }
    }

    protected fun initChat(playerRepository: PlayerRepository, channelId: String?, channelName: String, streamChatCallback: (() -> Unit)? = null) {
        channelId?.let { id ->
            call(playerRepository.loadSubscriberBadges(id)
                    .subscribeBy(onSuccess = {
                        it.badges
                        subscriberBadges = it
                        streamChatCallback?.invoke()
                    }, onError = {
                        //no subscriber badges
                        streamChatCallback?.invoke()
                    }))
        }
        call(playerRepository.loadBttvEmotes(channelName)
                .subscribeBy(onSuccess = { _bttv.value = it.body()?.emotes }))
        call(playerRepository.loadFfzEmotes(channelName)
                .subscribeBy(onSuccess = { _ffz.value = it.body()?.emotes }))
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
        Log.e("PlayerViewModel", "Player error", error)
        val context = getApplication<Application>()
        if (context.isNetworkAvailable) {
            try {
                val isStreamEnded = try {
                    error.type == ExoPlaybackException.TYPE_SOURCE &&
                            this@PlayerViewModel is StreamPlayerViewModel &&
                            error.sourceException.let { it is HttpDataSource.InvalidResponseCodeException && it.responseCode == 404 }
                } catch (e: IllegalStateException) {
                    Crashlytics.log(Log.ERROR, "PlayerViewModel", "onPlayerError: Stream end check error. Type: ${error.type}")
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
                            Crashlytics.log(Log.ERROR, "PlayerViewModel", "onPlayerError: Retry error. ${e.message}")
                            Crashlytics.logException(e)
                        }
                    }
                }
            } catch (e: Exception) {
                Crashlytics.log(Log.ERROR, "PlayerViewModel", "onPlayerError ${e.message}")
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