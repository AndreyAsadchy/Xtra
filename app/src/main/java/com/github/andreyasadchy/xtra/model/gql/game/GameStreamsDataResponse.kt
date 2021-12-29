package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.stream.Stream

data class GameStreamsDataResponse(val data: List<Stream>, val cursor: String?)