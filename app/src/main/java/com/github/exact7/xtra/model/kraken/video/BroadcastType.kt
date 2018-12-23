package com.github.exact7.xtra.model.kraken.video

enum class BroadcastType(val value: String) {
    ALL("all"),
    ARCHIVE("archive"),
    HIGHLIGHT("highlight"),
    UPLOAD("upload");

    override fun toString() = value
}