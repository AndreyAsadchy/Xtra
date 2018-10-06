package com.exact.xtra.ui.games

import com.exact.xtra.model.game.Game
import com.exact.xtra.repository.TwitchService
import com.exact.xtra.ui.common.PagedListViewModel

import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    init {
        loadData(repository.loadTopGames(compositeDisposable))
    }
}
