package com.github.andreyasadchy.xtra.model.gql.channel

import com.github.andreyasadchy.xtra.model.helix.clip.Clip

data class ChannelClipsDataResponse(val data: List<Clip>, val cursor: String?)