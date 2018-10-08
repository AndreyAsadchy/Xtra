package com.exact.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.exact.xtra.api.KrakenApi
import com.exact.xtra.model.game.Game
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class GamesDataSource(
        private val api: KrakenApi,
        retryExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePositionalDataSource<Game>(retryExecutor) {

    override fun loadInitial(params: PositionalDataSource.LoadInitialParams, callback: PositionalDataSource.LoadInitialCallback<Game>) {
        super.loadInitial(params, callback)
        api.getTopGames(params.requestedLoadSize, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.games) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadRange(params: PositionalDataSource.LoadRangeParams, callback: PositionalDataSource.LoadRangeCallback<Game>) {
        super.loadRange(params, callback)
        api.getTopGames(params.loadSize, params.startPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.games) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<Int, Game, GamesDataSource>() {

        override fun create(): DataSource<Int, Game> = GamesDataSource(api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
