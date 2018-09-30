package com.exact.twitch.ui.games

import com.exact.twitch.model.game.Game
import com.exact.twitch.repository.TwitchService
import com.exact.twitch.ui.viewmodel.PagedListViewModel

import javax.inject.Inject

class GamesViewModel @Inject
internal constructor(repository: TwitchService) : PagedListViewModel<Game>(repository) {

    init {
        loadData(repository.getTopGames(), false)
    }
}
