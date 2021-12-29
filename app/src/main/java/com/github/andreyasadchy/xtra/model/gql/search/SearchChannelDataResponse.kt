package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.channel.Channel

data class SearchChannelDataResponse(val data: List<Channel>, val cursor: String?)