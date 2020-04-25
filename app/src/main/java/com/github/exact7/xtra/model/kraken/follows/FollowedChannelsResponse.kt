package com.github.exact7.xtra.model.kraken.follows


import com.google.gson.annotations.SerializedName

class FollowedChannelsResponse(
        val follows: List<Follow>,
        @SerializedName("_total")
        val total: Int)