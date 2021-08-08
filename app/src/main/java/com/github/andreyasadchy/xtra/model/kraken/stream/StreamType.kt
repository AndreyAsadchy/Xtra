package com.github.andreyasadchy.xtra.model.kraken.stream

import com.google.gson.annotations.SerializedName

enum class StreamType(val value: String) {
    @SerializedName("live")
    LIVE("live"),
    @SerializedName("rerun")
    PLAYLIST("playlist"),
    @SerializedName("all")
    ALL("all");

    override fun toString() = value
}