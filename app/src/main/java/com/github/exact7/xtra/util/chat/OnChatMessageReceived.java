package com.github.exact7.xtra.util.chat;

import com.github.exact7.xtra.model.chat.ChatMessage;

public interface OnChatMessageReceived {
    void onMessage(ChatMessage message);
}
