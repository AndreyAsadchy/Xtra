package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameClipsQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import kotlinx.coroutines.CoroutineScope

class GameClipsDataSourceGQLquery(
    private val clientId: String?,
    private val gameId: String?,
    private val gameName: String?,
    private val sort: ClipsPeriod?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(GameClipsQuery(id = Optional.Present(gameId), sort = Optional.Present(sort), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.game?.clips
            val get = get1?.edges
            val list = mutableListOf<Clip>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = i?.node?.broadcaster?.id,
                            broadcaster_login = i?.node?.broadcaster?.login,
                            broadcaster_name = i?.node?.broadcaster?.displayName,
                            video_id = i?.node?.video?.id,
                            videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                            game_id = gameId,
                            game_name = gameName,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = i?.node?.broadcaster?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(GameClipsQuery(id = Optional.Present(gameId), sort = Optional.Present(sort), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.game?.clips
            val get = get1?.edges
            val list = mutableListOf<Clip>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = i?.node?.broadcaster?.id,
                            broadcaster_login = i?.node?.broadcaster?.login,
                            broadcaster_name = i?.node?.broadcaster?.displayName,
                            video_id = i?.node?.video?.id,
                            videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                            game_id = gameId,
                            game_name = gameName,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = i?.node?.broadcaster?.profileImageURL,
                        )
                    )
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
        private val sort: ClipsPeriod?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, GameClipsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Clip> =
                GameClipsDataSourceGQLquery(clientId, gameId, gameName, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
