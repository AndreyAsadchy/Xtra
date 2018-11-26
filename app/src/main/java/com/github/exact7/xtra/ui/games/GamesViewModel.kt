package com.github.exact7.xtra.ui.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    override val result: LiveData<Listing<Game>> = MutableLiveData<Listing<Game>>().apply {
        repository.loadTopGames(compositeDisposable)
    }
}