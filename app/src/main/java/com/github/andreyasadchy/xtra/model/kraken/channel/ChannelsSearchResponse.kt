package com.github.andreyasadchy.xtra.model.kraken.channel

import com.google.gson.annotations.SerializedName

class ChannelsSearchResponse(
        @SerializedName("_total")
        val total: Int,
        val channels: List<Channel>)