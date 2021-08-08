package com.github.andreyasadchy.xtra.model.kraken.follows

import com.google.gson.annotations.SerializedName

enum class Sort(val value: String) {
    @SerializedName("created_at")
    FOLLOWED_AT("created_at"),
    @SerializedName("login")
    ALPHABETICALLY("login"),
    LAST_BROADCAST("last_broadcast");

    override fun toString() = value
}