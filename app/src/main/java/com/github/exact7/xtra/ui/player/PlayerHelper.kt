package com.github.exact7.xtra.ui.player

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.util.chat.OnChatMessageReceived

class PlayerHelper : OnChatMessageReceived {

    val qualities: MutableLiveData<List<CharSequence>> = MutableLiveData()
    val chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList()}
    }
    val newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val urls: MutableMap<CharSequence, String> = LinkedHashMap()
    var selectedQualityIndex = 0

    override fun onMessage(message: ChatMessage) {
        chatMessages.value?.add(message)
        newMessage.postValue(message)
    }
}