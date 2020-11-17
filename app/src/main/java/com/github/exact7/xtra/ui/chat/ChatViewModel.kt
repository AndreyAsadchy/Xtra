package com.github.exact7.xtra.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Chatter
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import com.github.exact7.xtra.ui.player.ChatReplayManager
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.ui.view.chat.MAX_LIST_COUNT
import com.github.exact7.xtra.util.SingleLiveEvent
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import com.github.exact7.xtra.util.nullIfEmpty
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.set
import com.github.exact7.xtra.model.kraken.user.Emote as TwitchEmote

class ChatViewModel @Inject constructor(
        private val repository: TwitchService,
        private val playerRepository: PlayerRepository) : BaseViewModel(), ChatView.MessageSenderCallback {

    val recentEmotes: LiveData<List<Emote>> by lazy {
        MediatorLiveData<List<Emote>>().apply {
            addSource(twitchEmotes) { twitch ->
                removeSource(twitchEmotes)
                addSource(_otherEmotes) { other ->
                    removeSource(_otherEmotes)
                    addSource(playerRepository.loadRecentEmotes()) { recent ->
                        value = recent.filter { (twitch.contains<Emote>(it) || other.contains(it)) }
                    }
                }
            }
        }
    }
    val twitchEmotes by lazy { playerRepository.loadEmotes() }
    private val _otherEmotes = MutableLiveData<List<Emote>>()
    val otherEmotes: LiveData<List<Emote>>
        get() = _otherEmotes

    private val _chatMessages by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = Collections.synchronizedList(ArrayList(MAX_LIST_COUNT)) }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    private var subscriberBadges: SubscriberBadgesResponse? = null

    private var chat: ChatController? = null

    private val _newChatter by lazy { SingleLiveEvent<Chatter>() }
    val newChatter: LiveData<Chatter>
        get() = _newChatter

    val chatters: Collection<Chatter>
        get() = (chat as LiveChatController).chatters.values

    fun startLive(user: User, channel: Channel) {
        if (chat == null) {
            chat = LiveChatController(user, channel.name, channel.displayName)
            init(channel.id, channel.name)
        }
    }

    fun startReplay(channel: Channel, videoId: String, startTime: Double, getCurrentPosition: () -> Double) {
        if (chat == null) {
            chat = VideoChatController(videoId, startTime, getCurrentPosition)
            init(channel.id, channel.name)
        }
    }

    fun start() {
        chat?.start()
    }

    fun stop() {
        chat?.pause()
    }

    override fun send(message: CharSequence) {
        chat?.send(message)
    }

    override fun onCleared() {
        chat?.stop()
        super.onCleared()
    }

    private fun init(channelId: String, channelName: String) {
        viewModelScope.launch {
            try {
                subscriberBadges = playerRepository.loadSubscriberBadges(channelId)
            } catch (e: Exception) {
                //no subscriber badges
            } finally {
                chat?.start()
            }

            val list = mutableListOf<Emote>()
            globalBttvEmotes.also {
                if (it != null) {
                    list.addAll(it)
                } else {
                    try {
                        val emotes = playerRepository.loadGlobalBttvEmotes().body()?.emotes
                        if (emotes != null) {
                            globalBttvEmotes = emotes
                            list.addAll(emotes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global BTTV emotes", e)
                    }
                }
            }
            globalFfzEmotes.also {
                if (it != null) {
                    list.addAll(it)
                } else {
                    try {
                        val emotes = playerRepository.loadGlobalFfzEmotes().body()?.emotes
                        if (emotes != null) {
                            globalFfzEmotes = emotes
                            list.addAll(emotes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load global FFZ emotes", e)
                    }
                }
            }
            try {
                val channelBttv = playerRepository.loadBttvEmotes(channelName)
                channelBttv.body()?.emotes?.let(list::addAll)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load BTTV emotes for channel $channelName", e)
            }
            try {
                val channelFfz = playerRepository.loadFfzEmotes(channelName)
                channelFfz.body()?.emotes?.let(list::addAll)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load FFZ emotes for channel $channelName", e)
            }
            val sorted = list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            (chat as? LiveChatController)?.addEmotes(sorted)
            _otherEmotes.postValue(sorted)
        }
    }

    private inner class LiveChatController(
            private val user: User,
            private val channelName: String,
            displayName: String) : ChatController() {

        private var chat: LiveChatThread? = null
        private val allEmotesMap = mutableMapOf<String, Emote>()
        private var localEmotesObserver: Observer<List<TwitchEmote>>? = null

        val chatters = ConcurrentHashMap<String, Chatter>()

        init {
            if (user is LoggedIn) {
                localEmotesObserver = Observer<List<TwitchEmote>> { addEmotes(it) }.also(twitchEmotes::observeForever)
            }
            chatters[displayName] = Chatter(displayName)
        }

        override fun send(message: CharSequence) {
            chat?.send(message)
            val usedEmotes = hashSetOf<RecentEmote>()
            val currentTime = System.currentTimeMillis()
            message.split(' ').forEach { word ->
                allEmotesMap[word]?.let { usedEmotes.add(RecentEmote(word, it.url, currentTime)) }
            }
            if (usedEmotes.isNotEmpty()) {
                playerRepository.insertRecentEmotes(usedEmotes)
            }
        }

        override fun start() {
            pause()
            chat = TwitchApiHelper.startChat(channelName, user.name.nullIfEmpty(), user.token.nullIfEmpty(), subscriberBadges, this)
        }

        override fun pause() {
            chat?.disconnect()
        }

        override fun stop() {
            pause()
            localEmotesObserver?.let(twitchEmotes::removeObserver)
        }

        override fun onMessage(message: ChatMessage) {
            super.onMessage(message)
            if (!chatters.containsKey(message.displayName)) {
                val chatter = Chatter(message.displayName)
                chatters[message.displayName] = chatter
                _newChatter.postValue(chatter)
            }
        }

        fun addEmotes(list: List<Emote>) {
            if (user is LoggedIn) {
                allEmotesMap.putAll(list.associateBy { it.name })
            }
        }
    }

    private inner class VideoChatController(
            private val videoId: String,
            private val startTime: Double,
            private val getCurrentPosition: () -> Double) : ChatController() {

        private var chatReplayManager: ChatReplayManager? = null

        override fun send(message: CharSequence) {

        }

        override fun start() {
            stop()
            chatReplayManager = ChatReplayManager(repository, videoId, startTime, getCurrentPosition, this, { _chatMessages.postValue(ArrayList()) }, viewModelScope)
        }

        override fun pause() {
            chatReplayManager?.stop()
        }

        override fun stop() {
            chatReplayManager?.stop()
        }
    }

    private abstract inner class ChatController : OnChatMessageReceivedListener {
        abstract fun send(message: CharSequence)
        abstract fun start()
        abstract fun pause()
        abstract fun stop()

        override fun onMessage(message: ChatMessage) {
            message.badges?.find { it.id == "subscriber" }?.let {
                message.subscriberBadge = subscriberBadges?.getBadge(it.version.toInt())
            }
            _chatMessages.value!!.add(message)
            _newMessage.postValue(message)
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"

        private var globalBttvEmotes: List<BttvEmote>? = null
        private var globalFfzEmotes: List<FfzEmote>? = null
    }
}