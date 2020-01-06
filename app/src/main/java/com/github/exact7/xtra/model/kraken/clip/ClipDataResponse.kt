package com.github.exact7.xtra.model.kraken.clip


import com.google.gson.annotations.SerializedName

data class ClipDataResponse(
    val `data`: Data,
    val extensions: Extensions) {

    data class Data(val clip: Clip) {

        data class Clip(
            val id: String,
            @SerializedName("__typename")
            val typename: String,
            val videoQualities: List<VideoQuality>) {

            data class VideoQuality(
                val frameRate: Int,
                val quality: String,
                @SerializedName("sourceURL")
                val url: String,
                @SerializedName("__typename")
                val typename: String)
        }
    }

    data class Extensions(
        val durationMilliseconds: Int,
        val operationName: String)
}