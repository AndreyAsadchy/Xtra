package com.github.exact7.xtra.ui.search.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import com.github.exact7.xtra.util.toLiveData
import javax.inject.Inject

class GameSearchViewModel @Inject constructor(
        private val repository: TwitchService) : BaseViewModel() {

    private val query = MutableLiveData<String>()
    val list: LiveData<List<Game>> = Transformations.switchMap(query) {
        repository.loadGames(it, compositeDisposable)
                .doOnSubscribe { _loadingState.value = LoadingState.LOADING }
                .doOnEvent { _, _ -> _loadingState.value = LoadingState.LOADED }
                .toLiveData()
    }

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    fun setQuery(query: String) {
        if (this.query.value != query) {
            this.query.value = query
        }
    }
}