package com.github.andreyasadchy.xtra.model.kraken.clip

import com.google.gson.annotations.SerializedName

class ClipsResponse(
        val clips: List<Clip>,
        @SerializedName("_cursor")
        val cursor: String)