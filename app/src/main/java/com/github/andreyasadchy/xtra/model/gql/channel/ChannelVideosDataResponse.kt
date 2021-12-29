package com.github.andreyasadchy.xtra.model.gql.channel

import com.github.andreyasadchy.xtra.model.helix.video.Video

data class ChannelVideosDataResponse(val data: List<Video>, val cursor: String?)