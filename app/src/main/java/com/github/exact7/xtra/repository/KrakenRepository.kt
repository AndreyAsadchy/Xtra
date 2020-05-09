package com.github.exact7.xtra.repository

import android.util.Log
import androidx.paging.PagedList
import com.github.exact7.xtra.api.GraphQLApi
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.model.chat.VideoMessagesResponse
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.follows.Follow
import com.github.exact7.xtra.model.kraken.follows.Order
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.model.kraken.user.Emote
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Period
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.datasource.ChannelVideosDataSource
import com.github.exact7.xtra.repository.datasource.ChannelsSearchDataSource
import com.github.exact7.xtra.repository.datasource.ClipsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedChannelsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedClipsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedStreamsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedVideosDataSource
import com.github.exact7.xtra.repository.datasource.GamesDataSource
import com.github.exact7.xtra.repository.datasource.StreamsDataSource
import com.github.exact7.xtra.repository.datasource.VideosDataSource
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KrakenRepository"

@Singleton
class KrakenRepository @Inject constructor(
        private val api: KrakenApi,
        private val graphQL: GraphQLApi,
        private val emotesDao: EmotesDao,
        private val networkExecutor: Executor) : TwitchService {

    override fun loadTopGames(): Listing<GameWrapper> {
        val factory = GamesDataSource.Factory(api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadStream(channelId: String): StreamWrapper = withContext(Dispatchers.IO) {
        StreamWrapper(api.getStream(channelId).streams.firstOrNull())
    }

    override fun loadStreams(game: String?, languages: String?, streamType: StreamType): Listing<Stream> {
        val factory = StreamsDataSource.Factory(game, languages, streamType, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(userToken: String, streamType: StreamType, thumbnailsEnabled: Boolean): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(userToken, streamType, api, networkExecutor)
        val builder = PagedList.Config.Builder().setEnablePlaceholders(false)
        if (thumbnailsEnabled) {
            builder.setPageSize(10)
                    .setInitialLoadSizeHint(15)
                    .setPrefetchDistance(3)
        } else {
            builder.setPageSize(30)
                    .setInitialLoadSizeHint(30)
                    .setPrefetchDistance(10)
        }
        val config = builder.build()
        return Listing.create(factory, config)
    }

    override fun loadClips(channelName: String?, gameName: String?, languages: String?, period: com.github.exact7.xtra.model.kraken.clip.Period?, trending: Boolean): Listing<Clip> {
        val factory = ClipsDataSource.Factory(channelName, gameName, languages, period, trending, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedClips(userToken: String, trending: Boolean): Listing<Clip> {
        val factory = FollowedClipsDataSource.Factory(userToken, trending, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideo(videoId: String): Video = withContext(Dispatchers.IO) {
        api.getVideo(videoId)
    }

    override fun loadVideos(game: String?, period: Period, broadcastType: BroadcastType, language: String?, sort: Sort): Listing<Video> {
        val factory = VideosDataSource.Factory(game, period, broadcastType, language, sort, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort): Listing<Video> {
        val factory = FollowedVideosDataSource.Factory(userToken, broadcastType, language, sort, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideos(channelId: String, broadcastType: BroadcastType, sort: Sort): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(channelId, broadcastType, sort, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserById(id: Int): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by id $id")
        api.getUserById(id)
    }

    override suspend fun loadUserByLogin(login: String): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by login $login")
        api.getUsersByLogin(login).users.first()
    }

    override suspend fun loadUserEmotes(token: String, userId: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user emotes")
        val emotes = api.getUserEmotes(TwitchApiHelper.addTokenPrefix(token), userId).emotes.toMutableList()
        var modified = 0
        for (i in 0 until emotes.size) {
            val emote = emotes[i]
            if (emote.id in 1..14) {
                val value = when (emote.id) {
                    1 -> ":)"
                    2 -> ":("
                    3 -> ":D"
                    4 -> ">("
                    5 -> ":|"
                    6 -> "o_O"
                    7 -> "B)"
                    8 -> ":O"
                    9 -> "<3"
                    10 -> ":/"
                    11 -> ";)"
                    12 -> ":P"
                    13 -> ";P"
                    else -> "R)"
                }
                emotes[i] = Emote(emotes[i].id, value)
                if (++modified == 14) {
                    break
                }
            }
        }
        emotesDao.deleteAllAndInsert(emotes)
    }

    override fun loadChannels(query: String): Listing<Channel> {
        Log.d(TAG, "Loading channels containing: $query")
        val factory = ChannelsSearchDataSource.Factory(query, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(15)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideoChatLog(videoId: String, offsetSeconds: Double): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Offset in seconds: $offsetSeconds")
        api.getVideoChatLog(videoId.substring(1), offsetSeconds, 100)
    }

    override suspend fun loadVideoChatAfter(videoId: String, cursor: String): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Cursor: $cursor")
        api.getVideoChatLogAfter(videoId.substring(1), cursor, 100)
    }

    override suspend fun loadUserFollows(userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading if user is following channel $channelId")
        api.getUserFollows(userId, channelId).body()?.let { it.string().length > 300 } == true
    }

//    override suspend fun followChannel(userToken: String, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
//        Log.d(TAG, "Following channel $channelId")
//        api.followChannel(TwitchApiHelper.addTokenPrefix(userToken), userId, channelId).body() != null
//    }
    override suspend fun followChannel(userToken: String, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Following channel $channelId")
        val array = JsonArray(1)
        val followOperation = JsonObject().apply {
            addProperty("operationName", "FollowButton_FollowUser")
            add("variables", JsonObject().apply {
                add("input", JsonObject().apply {
                    addProperty("disableNotifications", false)
                    addProperty("targetID", channelId)
                })
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "3efee1acda90efdff9fef6e6b4a29213be3ee490781c5b54469717b6131ffdfe")
                })
            })
        }
        array.add(followOperation)
        graphQL.followChannel(TwitchApiHelper.addTokenPrefix(userToken), array).code() == 200
    }

    override suspend fun unfollowChannel(userToken: String, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Unfollowing channel $channelId")
        api.unfollowChannel(TwitchApiHelper.addTokenPrefix(userToken), userId, channelId).code() == 204
    }

    override suspend fun loadGames(query: String): List<Game> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading games containing: $query")
        api.getGames(query).games ?: emptyList()
    }

    override fun loadFollowedChannels(userId: String, sort: com.github.exact7.xtra.model.kraken.follows.Sort, order: Order): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(userId, sort, order, api, networkExecutor)
        val config = PagedList.Config.Builder()
                .setPageSize(40)
                .setInitialLoadSizeHint(40)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }
}