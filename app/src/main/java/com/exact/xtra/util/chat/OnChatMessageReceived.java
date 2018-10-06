package com.exact.xtra.util.chat;

import com.exact.xtra.model.chat.ChatMessage;

public interface OnChatMessageReceived {
    void onMessage(ChatMessage message);
}
