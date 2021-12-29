package com.github.andreyasadchy.xtra.model.helix.game

data class GamesResponse(
    val data: List<Game>,
    val pagination: Pagination?)

data class Pagination(
    val cursor: String?)