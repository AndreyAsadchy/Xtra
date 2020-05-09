package com.github.exact7.xtra.model.chat

import com.github.exact7.xtra.util.C
import com.google.gson.annotations.SerializedName

class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int,
        var end: Int) : Emote() {

    override val url: String
        get() = "${C.TWITCH_EMOTES_URL}$name/2.0"
}
