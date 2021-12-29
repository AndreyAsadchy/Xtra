package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameVideosQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.BroadcastType
import com.github.andreyasadchy.xtra.type.VideoSort
import kotlinx.coroutines.CoroutineScope

class GameVideosDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val gameId: String?,
    private val gameName: String?,
    private val type: BroadcastType?,
    private val sort: VideoSort?,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true
    private val typelist = mutableListOf<BroadcastType>()

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            if (type != null) typelist.add(type)
            val get1 = apolloClient(XtraModule(), clientId).query(GameVideosQuery(id = Optional.Present(gameId), sort = Optional.Present(sort), type = Optional.Present(typelist), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.game?.videos
            val get = get1?.edges
            val list = mutableListOf<Video>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = i?.node?.owner?.id,
                            user_login = i?.node?.owner?.login,
                            user_name = i?.node?.owner?.displayName,
                            game_id = gameId,
                            game_name = gameName,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = i?.node?.owner?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            if (type != null) typelist.add(type)
            val get1 = apolloClient(XtraModule(), clientId).query(GameVideosQuery(id = Optional.Present(gameId), sort = Optional.Present(sort), type = Optional.Present(typelist), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.game?.videos
            val get = get1?.edges
            val list = mutableListOf<Video>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = i?.node?.owner?.id,
                            user_login = i?.node?.owner?.login,
                            user_name = i?.node?.owner?.displayName,
                            game_id = gameId,
                            game_name = gameName,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = i?.node?.owner?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    class Factory (
        private val clientId: String?,
        private val gameId: String?,
        private val gameName: String?,
        private val type: BroadcastType?,
        private val sort: VideoSort?,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, GameVideosDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Video> =
                GameVideosDataSourceGQLquery(clientId, gameId, gameName, type, sort, coroutineScope).also(sourceLiveData::postValue)
    }
}
