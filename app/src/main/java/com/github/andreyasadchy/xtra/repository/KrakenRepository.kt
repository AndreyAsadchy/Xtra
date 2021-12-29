package com.github.andreyasadchy.xtra.repository

import android.util.Log
import androidx.paging.PagedList
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.StreamQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.db.EmotesDao
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.stream.StreamsResponse
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.*
import com.github.andreyasadchy.xtra.repository.datasource.*
import com.github.andreyasadchy.xtra.repository.datasourceGQL.SearchChannelsDataSourceGQL
import com.github.andreyasadchy.xtra.repository.datasourceGQL.SearchGamesDataSourceGQL
import com.github.andreyasadchy.xtra.repository.datasourceGQLquery.*
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ApiRepository"

@Singleton
class HelixRepository @Inject constructor(
    private val api: HelixApi,
    private val gql: GraphQLRepository,
    private val misc: MiscApi,
    private val emotesDao: EmotesDao) : TwitchService {

    override fun loadTopGames(clientId: String?, userToken: String?, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = GamesDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadStream(clientId: String?, userToken: String?, channelId: String): StreamsResponse = withContext(Dispatchers.IO) {
        StreamsResponse(api.getStream(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, null).data, null)
    }

    override fun loadStreams(clientId: String?, userToken: String?, game: String?, languages: String?, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, game, languages, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(clientId: String?, userToken: String?, user_id: String, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, user_id, api, coroutineScope)
        val builder = PagedList.Config.Builder().setEnablePlaceholders(false)
        if (thumbnailsEnabled) {
            builder.setPageSize(1)
                    .setInitialLoadSizeHint(1)
                    .setPrefetchDistance(3)
        } else {
            builder.setPageSize(1)
                    .setInitialLoadSizeHint(1)
                    .setPrefetchDistance(10)
        }
        val config = builder.build()
        return Listing.create(factory, config)
    }

    override fun loadClips(clientId: String?, userToken: String?, channelName: String?, gameName: String?, started_at: String?, ended_at: String?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ClipsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelName, gameName, started_at, ended_at, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideo(clientId: String?, userToken: String?, videoId: String): VideosResponse = withContext(Dispatchers.IO) {
        VideosResponse(api.getVideo(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, videoId).data, null)
    }

    override fun loadVideos(clientId: String?, userToken: String?, game: String?, period: Period, broadcastType: BroadcastType, language: String?, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = VideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, game, period, broadcastType, language, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideos(clientId: String?, userToken: String?, channelId: String, period: Period, broadcastType: BroadcastType, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, period, broadcastType, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserById(clientId: String?, userToken: String?, id: String): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by id $id")
        api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, id).data?.first()!!
    }

    override suspend fun loadUserByLogin(clientId: String?, userToken: String?, login: String): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by login $login")
        api.getUsersByLogin(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, login).data?.first()!!
    }

    override fun loadGames(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<Game> {
        Log.d(TAG, "Loading games containing: $query")
        val factory = GamesSearchDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, query, api, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannels(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<Channel> {
        Log.d(TAG, "Loading channels containing: $query")
        val factory = ChannelsSearchDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, query, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserFollows(clientId: String?, userToken: String?, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading if user is following channel $channelId")
        api.getUserFollows(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, channelId).total == 1
    }

    override fun loadFollowedChannels(clientId: String?, userToken: String?, userId: String, coroutineScope: CoroutineScope): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(1)
                .setInitialLoadSizeHint(1)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideoChatLog(clientId: String?, videoId: String, offsetSeconds: Double): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Offset in seconds: $offsetSeconds")
        misc.getVideoChatLog(clientId, videoId, offsetSeconds, 100)
    }

    override suspend fun loadVideoChatAfter(clientId: String?, videoId: String, cursor: String): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Cursor: $cursor")
        misc.getVideoChatLogAfter(clientId, videoId, cursor, 100)
    }

    override suspend fun loadEmotesFromSet(clientId: String?, userToken: String?, setId: String): List<TwitchEmote> = withContext(Dispatchers.IO) {
        api.getEmotesFromSet(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, setId).emotes
    }


    override suspend fun loadStreamGQL(clientId: String?, channelId: String): Int? = withContext(Dispatchers.IO) {
        apolloClient(XtraModule(), clientId).query(StreamQuery(Optional.Present(channelId))).execute().data?.user?.stream?.viewersCount
    }

    override fun loadTopGamesGQL(clientId: String?, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = GamesDataSourceGQLquery.Factory(clientId, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadTopStreamsGQL(clientId: String?, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSourceGQLquery.Factory(clientId, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadTopVideosGQL(clientId: String?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = VideosDataSourceGQLquery.Factory(clientId, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameStreamsGQL(clientId: String?, gameId: String?, gameName: String?, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = GameStreamsDataSourceGQLquery.Factory(clientId, gameId, gameName, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameVideosGQL(clientId: String?, gameId: String?, gameName: String?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = GameVideosDataSourceGQLquery.Factory(clientId, gameId, gameName, type, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameClipsGQL(clientId: String?, gameId: String?, gameName: String?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = GameClipsDataSourceGQLquery.Factory(clientId, gameId, gameName, sort, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideosGQL(clientId: String?, game: String?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSourceGQLquery.Factory(clientId, game, type, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelClipsGQL(clientId: String?, game: String?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ChannelClipsDataSourceGQLquery.Factory(clientId, game, sort, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchChannelsGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<Channel> {
        val factory = SearchChannelsDataSourceGQL.Factory(clientId, query, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchGamesGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = SearchGamesDataSourceGQL.Factory(clientId, query, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }
}