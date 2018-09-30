package com.exact.twitch.ui.games

import com.exact.twitch.model.game.Game
import com.exact.twitch.repository.TwitchService
import com.exact.twitch.ui.common.PagedListViewModel

import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    init {
        loadData(repository.loadTopGames(compositeDisposable))
    }
}
