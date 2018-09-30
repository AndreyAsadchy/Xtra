package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.game.Game
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class GamesDataSource(
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Game>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Game>) {
        super.loadInitial(params, callback)
        api.getTopGames(params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.games) },{ callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Game>) {
        super.loadRange(params, callback)
        api.getTopGames(params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.games) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<Int, Game, GamesDataSource>() {

        override fun create(): DataSource<Int, Game> = GamesDataSource(api, networkExecutor).also(sourceLiveData::postValue)
    }
}
