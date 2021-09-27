package com.github.andreyasadchy.xtra.model.chat

class StvEmote(
    override val name: String,
    val mime: String,
    override val url: String,
    override val zerowidth: Boolean) : Emote() {

    override val isPng: String
        get() = mime
}