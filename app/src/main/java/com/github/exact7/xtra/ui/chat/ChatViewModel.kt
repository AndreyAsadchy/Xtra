package com.github.exact7.xtra.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import com.github.exact7.xtra.ui.player.ChatReplayManager
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.chat.EmotesUrlHelper
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import com.github.exact7.xtra.util.nullIfEmpty
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject
import com.github.exact7.xtra.model.kraken.user.Emote as TwitchEmote

class ChatViewModel @Inject constructor(
        private val repository: TwitchService,
        private val playerRepository: PlayerRepository): BaseViewModel(), OnChatMessageReceivedListener, ChatView.MessageSenderCallback {

    val emotes by lazy { playerRepository.loadEmotes() }
    val recentEmotes by lazy { playerRepository.loadRecentEmotes() }
    private val _bttv = MutableLiveData<List<BttvEmote>>()
    val bttv: LiveData<List<BttvEmote>>
        get() = _bttv
    private val _ffz = MutableLiveData<List<FfzEmote>>()
    val ffz: LiveData<List<FfzEmote>>
        get() = _ffz
    private val allEmotesMap: MutableMap<String, Emote> by lazy { hashMapOf<String, Emote>() }
    private var localEmotesObserver: Observer<List<TwitchEmote>>? = null

    private val _chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList() }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    private var subscriberBadges: SubscriberBadgesResponse? = null
    private var chatReplayManager: ChatReplayManager? = null

    private var isLive = true
    private lateinit var user: User
    private lateinit var channelName: String

    private lateinit var chat: LiveChatThread

    fun startLive(user: User, channelId: String, channelName: String) {
        if (!this::channelName.isInitialized) {
            this.user = user
            this.channelName = channelName
            init(channelId, channelName)
            if (user is LoggedIn) {
                localEmotesObserver = Observer<List<TwitchEmote>> { putEmotes(it) }.also(emotes::observeForever)
            }
        }
    }

    fun startReplay(channelId: String, channelName: String, videoId: String, startTime: Double, getCurrentPosition: () -> Double) {
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

    override fun send(message: CharSequence) {
        chat.send(message)
        val usedEmotes = hashSetOf<RecentEmote>()
        val currentTime = System.currentTimeMillis()
        message.split(' ').forEach { word -> //TODO it only inserts from one tab
            allEmotesMap[word]?.let { usedEmotes.add(RecentEmote(word, EmotesUrlHelper.resolveUrl(it), currentTime)) }
        }
        if (usedEmotes.isNotEmpty()) {
            playerRepository.insertRecentEmotes(usedEmotes)
        }
    }

    fun start() {
        if (isLive) {
            stop()
            chat = TwitchApiHelper.startChat(channelName, user.name.nullIfEmpty(), user.token.nullIfEmpty(), subscriberBadges, this)
        }
    }

    fun stop() {
        if (this::chat.isInitialized) {
            chat.disconnect()
        }
    }

    private fun init(channelId: String, channelName: String) {
        call(playerRepository.loadSubscriberBadges(channelId)
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
        call(playerRepository.loadBttvEmotes(channelName)
                .subscribeBy(onSuccess = { response ->
                    _bttv.value = response.body()?.let {
                        putEmotes(it.emotes)
                        it.emotes
                    }
                }))
        call(playerRepository.loadFfzEmotes(channelName)
                .subscribeBy(onSuccess = { response ->
                    _ffz.value = response.body()?.let {
                        putEmotes(it.emotes)
                        it.emotes
                    }
                }))
    }

    private fun clearMessages() {
        _chatMessages.postValue(ArrayList())
    }

    override fun onCleared() {
        if (isLive) {
            stop()
            localEmotesObserver?.let(emotes::removeObserver)
        } else {
            chatReplayManager?.stop()
        }
        super.onCleared()
    }

    private fun <T : Emote> putEmotes(list: List<T>) {
        allEmotesMap.putAll(list.associateBy { it.name })
    }

}