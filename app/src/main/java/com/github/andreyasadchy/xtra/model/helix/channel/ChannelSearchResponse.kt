package com.github.andreyasadchy.xtra.model.helix.channel

class ChannelSearchResponse(
    val data: List<Channel>,
    val pagination: Pagination?)

data class Pagination(
    val cursor: String?)