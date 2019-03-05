package com.github.exact7.xtra.model.chat

data class Image(
        val url: String,
        var start: Int,
        var end: Int,
        val isEmote: Boolean,
        val isPng: Boolean = true,
        val width: Float? = null)