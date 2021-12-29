package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserClipsQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import kotlinx.coroutines.CoroutineScope

class ChannelClipsDataSourceGQLquery(
    private val clientId: String?,
    private val game: String?,
    private val sort: ClipsPeriod?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(UserClipsQuery(Optional.Present(game), Optional.Present(sort), Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.user
            val get = get1?.clips?.edges
            val list = mutableListOf<Clip>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = get1.id,
                            broadcaster_login = get1.login,
                            broadcaster_name = get1.displayName,
                            video_id = i?.node?.video?.id,
                            videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                            game_id = i?.node?.game?.id,
                            game_name = i?.node?.game?.displayName,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = get1.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.clips.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(UserClipsQuery(Optional.Present(game), Optional.Present(sort), Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.user
            val get = get1?.clips?.edges
            val list = mutableListOf<Clip>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = get1.id,
                            broadcaster_login = get1.login,
                            broadcaster_name = get1.displayName,
                            video_id = i?.node?.video?.id,
                            videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                            game_id = i?.node?.game?.id,
                            game_name = i?.node?.game?.displayName,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = get1.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.clips.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val game: String?,
        private val sort: ClipsPeriod?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ChannelClipsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Clip> =
                ChannelClipsDataSourceGQLquery(clientId, game, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
