package com.github.andreyasadchy.xtra.model.kraken.video

enum class BroadcastType(val value: String) {
    ALL("all"),
    ARCHIVE("archive"),
    HIGHLIGHT("highlight"),
    UPLOAD("upload");

    override fun toString() = value
}