package com.exact.xtra.util.chat

import com.exact.xtra.tasks.LiveChatTask

interface OnChatConnectedListener {
    fun onConnect(chatTask: LiveChatTask)
}
