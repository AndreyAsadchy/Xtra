package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.game.Game

data class SearchGameDataResponse(val data: List<Game>, val cursor: String?)