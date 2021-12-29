package com.github.andreyasadchy.xtra.model.helix.video

enum class Sort(val value: String) {
    TIME("time"),
    VIEWS("views");

    override fun toString() = value
}