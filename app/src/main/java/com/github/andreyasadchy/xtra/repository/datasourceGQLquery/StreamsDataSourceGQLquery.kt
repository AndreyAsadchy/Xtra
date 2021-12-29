package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.TopStreamsQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class StreamsDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(TopStreamsQuery(Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.streams
            val get = get1?.edges
            val list = mutableListOf<Stream>()
            if (get != null) {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_id = i?.node?.game?.id,
                        game_name = i?.node?.game?.displayName,
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
            val get1 = apolloClient(XtraModule(), clientId).query(TopStreamsQuery(Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.streams
            val get = get1?.edges
            val list = mutableListOf<Stream>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_id = i?.node?.game?.id,
                        game_name = i?.node?.game?.displayName,
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
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSourceGQLquery(clientId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
