package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.game.Game
import kotlinx.coroutines.CoroutineScope

class GamesSearchDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val query: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            val get = api.getGames(clientId, userToken, query, params.requestedLoadSize, offset)
            offset = get.pagination?.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            val get = api.getGames(clientId, userToken, query, params.loadSize, offset)
            if (offset != null && offset != "") {
                offset = get.pagination?.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val query: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, GamesSearchDataSource>() {

        override fun create(): DataSource<Int, Game> =
                GamesSearchDataSource(clientId, userToken, query, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
