package com.exact.twitch.util.chat

import com.exact.twitch.tasks.LiveChatTask

interface OnChatConnectedListener {
    fun onConnect(chatTask: LiveChatTask)
}
