package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.annotations.SerializedName

private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote() {

    override val isPng: Boolean
        get() = imageType.endsWith("png", true)

    override val url: String
        get() = "$BTTV_URL$id/${emoteQuality}x"
}