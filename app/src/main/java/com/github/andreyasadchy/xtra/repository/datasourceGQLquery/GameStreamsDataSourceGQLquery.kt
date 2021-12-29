package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameStreamsQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class GameStreamsDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val gameId: String?,
    private val gameName: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(GameStreamsQuery(id = Optional.Present(gameId), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.game?.streams
            val get = get1?.edges
            val list = mutableListOf<Stream>()
            if (get != null) {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_id = gameId,
                        game_name = gameName,
                        type = i?.node?.type,
                        title = i?.node?.title,
                        viewer_count = i?.node?.viewersCount,
                        thumbnail_url = i?.node?.previewImageURL,
                        profileImageURL = i?.node?.broadcaster?.profileImageURL
                    ))
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(GameStreamsQuery(id = Optional.Present(gameId), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.game?.streams
            val get = get1?.edges
            val list = mutableListOf<Stream>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_id = gameId,
                        game_name = gameName,
                        type = i?.node?.type,
                        title = i?.node?.title,
                        viewer_count = i?.node?.viewersCount,
                        thumbnail_url = i?.node?.previewImageURL,
                        profileImageURL = i?.node?.broadcaster?.profileImageURL
                    ))
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val gameId: String?,
        private val gameName: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, GameStreamsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Stream> =
                GameStreamsDataSourceGQLquery(clientId, gameId, gameName, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
