package com.github.andreyasadchy.xtra.model.helix.video

class VideosResponse(
    val data: List<Video>,
    val pagination: Pagination?)

data class Pagination(
    val cursor: String?)