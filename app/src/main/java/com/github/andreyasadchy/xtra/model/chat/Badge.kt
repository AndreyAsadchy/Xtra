package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.annotations.SerializedName

data class Badge(
        @SerializedName("_id")
        val id: String,
        val version: String)
