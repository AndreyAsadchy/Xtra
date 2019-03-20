package com.github.exact7.xtra.util.chat;

import com.github.exact7.xtra.model.chat.ChatMessage;

public interface OnChatMessageReceivedListener {
    void onMessage(ChatMessage message);
}
