package com.github.exact7.xtra.util.chat

import com.github.exact7.xtra.model.chat.ChatMessage

interface OnChatMessageReceivedListener {
    fun onMessage(message: ChatMessage)
}
