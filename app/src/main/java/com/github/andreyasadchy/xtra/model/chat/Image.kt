package com.github.andreyasadchy.xtra.model.chat

data class Image(
        val url: String,
        var start: Int,
        var end: Int,
        val isEmote: Boolean,
        val isPng: Boolean = true,
        val zerowidth: Boolean = false)