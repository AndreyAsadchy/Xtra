package com.exact.twitch.model.stream

import com.google.gson.annotations.SerializedName

class StreamsResponse(
    @SerializedName("_total")
    val total: Int,
    val streams: List<Stream>)

