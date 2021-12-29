package com.github.andreyasadchy.xtra.model.helix.clip

class ClipsResponse(
    val data: List<Clip>,
    val pagination: Pagination?)

data class Pagination(
    val cursor: String?)