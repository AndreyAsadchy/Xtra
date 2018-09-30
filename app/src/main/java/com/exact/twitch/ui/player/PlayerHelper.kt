package com.exact.twitch.ui.player

import androidx.lifecycle.MutableLiveData
import com.exact.twitch.model.chat.ChatMessage
import com.exact.twitch.util.chat.OnChatMessageReceived

class PlayerHelper : OnChatMessageReceived {

    val qualities: MutableLiveData<List<CharSequence>> = MutableLiveData()
    val chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList()}
    }
    val newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val urls: MutableMap<CharSequence, String> = LinkedHashMap()
    var selectedQualityIndex: Int = 0 //TODO shared preferences

    override fun onMessage(message: ChatMessage) {
        chatMessages.value?.add(message)
        newMessage.postValue(message)
    }
}