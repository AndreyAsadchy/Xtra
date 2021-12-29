package com.github.andreyasadchy.xtra.model.helix.stream

class StreamsResponse(
    val data: List<Stream>,
    val pagination: Pagination?)

data class Pagination(
    val cursor: String?)