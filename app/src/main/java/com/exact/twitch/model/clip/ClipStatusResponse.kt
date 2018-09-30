package com.exact.twitch.model.clip

import com.google.gson.annotations.SerializedName

class ClipStatusResponse(
        val status: String,
        @SerializedName("quality_options")
        val qualityOptions: List<QualityOption>) {

    data class QualityOption(
            @SerializedName("quality")
            val quality: String,
            val source: String,
            @SerializedName("frame_rate")
            val frameRate: Int)
}
