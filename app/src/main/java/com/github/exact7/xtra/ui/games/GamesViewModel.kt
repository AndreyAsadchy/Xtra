package com.github.exact7.xtra.ui.games

import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel

import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    init {
        loadData(repository.loadTopGames(compositeDisposable))
    }
}
