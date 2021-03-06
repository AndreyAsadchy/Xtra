package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.game.GameWrapper
import kotlinx.coroutines.CoroutineScope

class GamesDataSource(
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<GameWrapper>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<GameWrapper>) {
        loadInitial(params, callback) {
            api.getTopGames(params.requestedLoadSize, 0).games
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<GameWrapper>) {
        loadRange(params, callback) {
            api.getTopGames(params.loadSize, params.startPosition).games
        }
    }

    class Factory(
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, GameWrapper, GamesDataSource>() {

        override fun create(): DataSource<Int, GameWrapper> = GamesDataSource(api, coroutineScope).also(sourceLiveData::postValue)
    }
}
