package com.github.andreyasadchy.xtra.ui.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.kraken.game.GameWrapper
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<GameWrapper>() {

    override val result: LiveData<Listing<GameWrapper>> = MutableLiveData<Listing<GameWrapper>>(repository.loadTopGames(viewModelScope))
}
