package com.exact.twitch.model.game

import com.google.gson.annotations.SerializedName

class GamesResponse(
    @SerializedName("_total")
    val total: Int,
    @SerializedName("top")
    val games: List<Game>)

