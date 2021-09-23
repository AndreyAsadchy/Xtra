package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.annotations.SerializedName

private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote() {

    override val isPng: String
        get() = "image/$imageType"

    override val url: String
        get() = "$BTTV_URL$id/2x"
}