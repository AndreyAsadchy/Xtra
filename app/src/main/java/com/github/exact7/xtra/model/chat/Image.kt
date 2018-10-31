package com.github.exact7.xtra.model.chat

data class Image(
        val url: String,
        val start: Int,
        val end: Int,
        val isEmote: Boolean)