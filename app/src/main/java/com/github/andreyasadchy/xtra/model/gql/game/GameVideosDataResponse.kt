package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.video.Video

data class GameVideosDataResponse(val data: List<Video>, val cursor: String?)