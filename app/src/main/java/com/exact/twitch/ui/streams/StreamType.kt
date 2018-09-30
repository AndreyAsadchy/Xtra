package com.exact.twitch.ui.streams

enum class StreamType(val value: String) {
    LIVE("live"), PLAYLIST("playlist"), ALL("all");

    override fun toString() = value
}