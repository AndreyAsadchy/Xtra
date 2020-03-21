package com.github.exact7.xtra.ui.search.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import javax.inject.Inject

class GameSearchViewModel @Inject constructor(
        private val repository: TwitchService) : BaseViewModel() {

    private val query = MutableLiveData<String>()
    val list: LiveData<List<Game>> = Transformations.switchMap(query) {
        liveData {
            try {
                _loadingState.postValue(LoadingState.LOADING)
                val games = repository.loadGames(it)
                emit(games)
            } catch (e: Exception) {
                shouldRetry = true
            } finally {
                _loadingState.postValue(LoadingState.LOADED)
            }
        }
    }
    private var shouldRetry = false

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    fun setQuery(query: String) {
        if (this.query.value != query) {
            this.query.value = query
        }
    }

    fun retry() {
        if (shouldRetry) {
            shouldRetry = false
            query.value = query.value
        }
    }
}