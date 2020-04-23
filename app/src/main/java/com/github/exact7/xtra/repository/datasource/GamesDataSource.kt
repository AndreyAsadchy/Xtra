package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import java.util.concurrent.Executor

class GamesDataSource(
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<GameWrapper>(retryExecutor) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<GameWrapper>) {
        loadInitial(params, callback) {
            api.getTopGames(params.requestedLoadSize, 0).execute().body()!!.games
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<GameWrapper>) {
        loadRange(params, callback) {
            api.getTopGames(params.loadSize, params.startPosition + 1).execute().body()!!.games
        }
    }

    class Factory(
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<Int, GameWrapper, GamesDataSource>() {

        override fun create(): DataSource<Int, GameWrapper> = GamesDataSource(api, retryExecutor).also(sourceLiveData::postValue)
    }
}
