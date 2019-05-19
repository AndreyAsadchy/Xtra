package com.github.exact7.xtra.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import com.github.exact7.xtra.ui.player.ChatReplayManager
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import com.github.exact7.xtra.util.nullIfEmpty
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class ChatViewModel @Inject constructor(
        private val repository: TwitchService,
        private val playerRepository: PlayerRepository): BaseViewModel(), OnChatMessageReceivedListener {

    val emotes by lazy { playerRepository.loadEmotes() }
    private val _bttv = MutableLiveData<List<BttvEmote>>()
    val bttv: LiveData<List<BttvEmote>>
        get() = _bttv
    private val _ffz = MutableLiveData<List<FfzEmote>>()
    val ffz: LiveData<List<FfzEmote>>
        get() = _ffz

    private val _chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList() }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    private val _chat by lazy { MutableLiveData<LiveChatThread>() }
    val chat: LiveData<LiveChatThread>
        get() = _chat

    private var subscriberBadges: SubscriberBadgesResponse? = null
    private var chatReplayManager: ChatReplayManager? = null

    private var isLive = true
    private lateinit var user: User
    private lateinit var channelName: String

    fun startLive(user: User, channelId: String?, channelName: String) {
        if (!this::channelName.isInitialized) {
            this.user = user
            this.channelName = channelName
            init(channelId, channelName)
        }
    }

    fun startReplay(channelId: String?, channelName: String, videoId: String, startTime: Double, getCurrentPosition: () -> Double) {
        if (chatReplayManager == null) {
            isLive = false
            init(channelId, channelName)
            chatReplayManager = ChatReplayManager(repository, videoId, startTime, getCurrentPosition, this::onMessage, this::clearMessages)
        }
    }

    override fun onMessage(message: ChatMessage) {
        message.badges?.find { it.id == "subscriber" }?.let {
            message.subscriberBadge = subscriberBadges?.getBadge(it.version.toInt())
        }
        _chatMessages.value?.add(message)
        _newMessage.postValue(message)
    }

    fun start() {
        if (isLive) {
            stop()
            _chat.value = TwitchApiHelper.startChat(channelName, user.name.nullIfEmpty(), user.token.nullIfEmpty(), subscriberBadges, this)
        }
    }

    fun stop() {
        if (isLive) {
            _chat.value?.disconnect()
        }
    }

    private fun init(channelId: String?, channelName: String) {
        channelId?.let { id ->
            call(playerRepository.loadSubscriberBadges(id)
                    .subscribeBy(onSuccess = {
                        it.badges
                        subscriberBadges = it
                        if (isLive) {
                            start()
                        }
                    }, onError = {
                        //no subscriber badges
                        if (isLive) {
                            start()
                        }
                    }))
        }
        call(playerRepository.loadBttvEmotes(channelName)
                .subscribeBy(onSuccess = { _bttv.value = it.body()?.emotes }))
        call(playerRepository.loadFfzEmotes(channelName)
                .subscribeBy(onSuccess = { _ffz.value = it.body()?.emotes }))
    }

    private fun clearMessages() {
        _chatMessages.postValue(ArrayList())
    }

    override fun onCleared() {
        if (isLive) {
            stop()
        } else {
            chatReplayManager?.stop()
        }
        super.onCleared()
    }
}