package com.github.andreyasadchy.xtra.model.gql.clip

data class ClipDataResponse(val videos: List<Video>) {

    data class Video(
            val frameRate: Int,
            val quality: String,
            val url: String)
}