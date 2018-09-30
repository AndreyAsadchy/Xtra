package com.exact.twitch.util.chat;

import com.exact.twitch.tasks.LiveChatTask;

@FunctionalInterface
public interface OnChatConnectedListener {
    void onConnect(LiveChatTask chatTask);
}
