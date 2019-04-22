package com.github.exact7.xtra.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
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
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

abstract class PlayerViewModel(context: Application) : AndroidViewModel(context), Player.EventListener, OnChatMessageReceivedListener {

    protected val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
    protected val trackSelector = DefaultTrackSelector()
    protected val compositeDisposable = CompositeDisposable()
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
            playerRepository.loadSubscriberBadges(id)
                    .subscribeBy(onSuccess = {
                        it.badges
                        subscriberBadges = it
                        streamChatCallback?.invoke()
                    }, onError = {
                        //no subscriber badges
                        streamChatCallback?.invoke()
                    })
                    .addTo(compositeDisposable)
        }
        playerRepository.loadBttvEmotes(channelName)
                .subscribeBy(onSuccess = { _bttv.value = it.body()?.emotes })
                .addTo(compositeDisposable)
        playerRepository.loadFfzEmotes(channelName)
                .subscribeBy(onSuccess = { _ffz.value = it.body()?.emotes })
                .addTo(compositeDisposable)
    }

    open fun onResume() {
        play()
    }

    open fun onPause() {
        player.stop()
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