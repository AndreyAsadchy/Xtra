package com.github.andreyasadchy.xtra.model.gql.stream

import com.github.andreyasadchy.xtra.model.helix.stream.Stream

data class StreamDataResponse(val data: List<Stream>, val cursor: String?)