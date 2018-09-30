package com.exact.twitch.util.chat;

import com.exact.twitch.model.chat.ChatMessage;

@FunctionalInterface
public interface OnChatMessageReceived {
    void onMessage(ChatMessage message);
}
