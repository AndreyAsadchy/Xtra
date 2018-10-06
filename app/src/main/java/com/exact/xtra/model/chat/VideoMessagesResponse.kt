package com.exact.xtra.model.chat

import com.google.gson.annotations.SerializedName

class VideoMessagesResponse(
        @SerializedName("comments")
        val messages: List<VideoChatMessage>,
        @SerializedName("_next")
        val next: String? = null)
