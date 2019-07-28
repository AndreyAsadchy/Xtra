package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class GamesDataSource(
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<GameWrapper>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<GameWrapper>) {
        super.loadInitial(params, callback)
        api.getTopGames(params.requestedLoadSize, 0)
                .subscribe({ callback.onSuccess(it.games) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<GameWrapper>) {
        super.loadRange(params, callback)
        api.getTopGames(params.loadSize, params.startPosition + 1)
                .subscribe({ callback.onSuccess(it.games) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, GameWrapper, GamesDataSource>() {

        override fun create(): DataSource<Int, GameWrapper> = GamesDataSource(api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
