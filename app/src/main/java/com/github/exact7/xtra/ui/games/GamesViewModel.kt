package com.github.exact7.xtra.ui.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<GameWrapper>() {

    override val result: LiveData<Listing<GameWrapper>> = MutableLiveData<Listing<GameWrapper>>().apply {
        value = repository.loadTopGames(viewModelScope)
    }
}
