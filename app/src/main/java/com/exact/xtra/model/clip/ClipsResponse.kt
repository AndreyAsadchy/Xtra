package com.exact.xtra.model.clip

import com.google.gson.annotations.SerializedName

class ClipsResponse(
        val clips: List<Clip>,
        @SerializedName("_cursor")
        val cursor: String)