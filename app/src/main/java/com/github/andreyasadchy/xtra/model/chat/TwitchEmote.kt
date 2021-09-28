package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.util.C
import com.google.gson.annotations.SerializedName

class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int,
        var end: Int,
        override val isPng: String = "image/png") : Emote() {

    override val url: String
        get() = "${C.TWITCH_EMOTES_URL}$name/default/dark/2.0"
}
