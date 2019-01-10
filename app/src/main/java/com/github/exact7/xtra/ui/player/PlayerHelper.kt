package com.github.exact7.xtra.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.util.chat.OnChatMessageReceived

class PlayerHelper : OnChatMessageReceived {

    var urls: Map<String, String>? = null
    var selectedQualityIndex = 0
    val loaded = MutableLiveData<Boolean>()

    private val _chatMessages: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().apply { value = ArrayList()}
    }
    val chatMessages: LiveData<MutableList<ChatMessage>>
        get() = _chatMessages

    private val _newMessage: MutableLiveData<ChatMessage> by lazy { MutableLiveData<ChatMessage>() }
    val newMessage: LiveData<ChatMessage>
        get() = _newMessage

    override fun onMessage(message: ChatMessage) {
        chatMessages.value?.add(message)
        _newMessage.postValue(message)
    }
}