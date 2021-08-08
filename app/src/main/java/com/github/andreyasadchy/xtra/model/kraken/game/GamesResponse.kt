package com.github.andreyasadchy.xtra.model.kraken.game

import com.google.gson.annotations.SerializedName

data class GamesResponse(
    @SerializedName("_total")
    val total: Int,
    @SerializedName("top")
    val games: List<GameWrapper>)

