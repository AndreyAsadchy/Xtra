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
import com.github.exact7.xtra.ui.view.chat.MAX_LIST_COUNT
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.chat.EmotesUrlHelper
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import com.github.exact7.xtra.util.nullIfEmpty
import io.reactivex.rxkotlin.addTo
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

    private val _chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList(MAX_LIST_COUNT) }
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages
    private val _newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    private var subscriberBadges: SubscriberBadgesResponse? = null

    private var chat: ChatController? = null

    fun startLive(user: User, channelId: String, channelName: String) {
        if (chat == null) {
            chat = LiveChatController(user, channelName)
            init(channelId, channelName)

        }
    }

    fun startReplay(channelId: String, channelName: String, videoId: String, startTime: Double, getCurrentPosition: () -> Double) {
        if (chat == null) {
            chat = VideoChatController(videoId, startTime, getCurrentPosition)
            init(channelId, channelName)
        }
    }

    fun start() {
        chat?.start()
    }

    fun stop() {
        chat?.pause()
    }

    override fun onMessage(message: ChatMessage) {
        message.badges?.find { it.id == "subscriber" }?.let {
            message.subscriberBadge = subscriberBadges?.getBadge(it.version.toInt())
        }
        _chatMessages.value!!.add(message)
        _newMessage.postValue(message)
    }

    override fun send(message: CharSequence) {
        chat?.send(message)
    }

    override fun onCleared() {
        chat?.stop()
        super.onCleared()
    }

    private fun init(channelId: String, channelName: String) {
        playerRepository.loadSubscriberBadges(channelId)
                .subscribeBy(onSuccess = {
                    subscriberBadges = it
                    chat?.start()
                }, onError = {
                    //no subscriber badges
                    chat?.start()
                })
                .addTo(compositeDisposable)
        playerRepository.loadBttvEmotes(channelName)
                .subscribeBy(onSuccess = { response ->
                    _bttv.value = response.body()?.let {
                        (chat as? LiveChatController)?.addEmotes(it.emotes)
                        it.emotes
                    }
                })
                .addTo(compositeDisposable)
        playerRepository.loadFfzEmotes(channelName)
                .subscribeBy(onSuccess = { response ->
                    _ffz.value = response.body()?.let {
                        (chat as? LiveChatController)?.addEmotes(it.emotes)
                        it.emotes
                    }
                })
                .addTo(compositeDisposable)
    }

    private inner class VideoChatController(
            private val videoId: String,
            private val startTime: Double,
            private val getCurrentPosition: () -> Double) : ChatController {

        private var chatReplayManager: ChatReplayManager? = null

        override fun send(message: CharSequence) {

        }

        override fun start() {
            chatReplayManager = ChatReplayManager(repository, videoId, startTime, getCurrentPosition, this@ChatViewModel, { _chatMessages.postValue(ArrayList()) })
        }

        override fun pause() {
            chatReplayManager?.stop()
        }

        override fun stop() {
            chatReplayManager?.stop()
        }
    }

    private inner class LiveChatController(
            private val user: User,
            private val channelName: String) : ChatController {

        private var chat: LiveChatThread? = null
        private val allEmotesMap: MutableMap<String, Emote> = ChatFragment.defaultBttvAndFfzEmotes().associateByTo(HashMap()) { it.name }
        private var localEmotesObserver: Observer<List<TwitchEmote>>? = null

        init {
            if (user is LoggedIn) {
                localEmotesObserver = Observer<List<TwitchEmote>> { addEmotes(it) }.also(emotes::observeForever)
            }
        }

        override fun send(message: CharSequence) {
            chat?.send(message)
            val usedEmotes = hashSetOf<RecentEmote>()
            val currentTime = System.currentTimeMillis()
            message.split(' ').forEach { word ->
                allEmotesMap[word]?.let { usedEmotes.add(RecentEmote(word, EmotesUrlHelper.resolveUrl(it), currentTime)) }
            }
            if (usedEmotes.isNotEmpty()) {
                playerRepository.insertRecentEmotes(usedEmotes)
            }
        }

        override fun start() {
            chat = TwitchApiHelper.startChat(channelName, user.name.nullIfEmpty(), user.token.nullIfEmpty(), subscriberBadges, this@ChatViewModel)
        }

        override fun pause() {
            chat?.disconnect()
        }

        override fun stop() {
            pause()
            localEmotesObserver?.let(emotes::removeObserver)
        }

        fun addEmotes(list: List<Emote>) {
            if (user is LoggedIn) {
                allEmotesMap.putAll(list.associateBy { it.name })
            }
        }
    }

    private interface ChatController {
        fun send(message: CharSequence)
        fun start()
        fun pause()
        fun stop()
    }
}