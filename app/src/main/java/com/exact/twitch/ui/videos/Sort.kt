package com.exact.twitch.ui.videos

enum class Sort(val value: String) {
    TIME("time"),
    VIEWS("views");

    override fun toString() = value
}