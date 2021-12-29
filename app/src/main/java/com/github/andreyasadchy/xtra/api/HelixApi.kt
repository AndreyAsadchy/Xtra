package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearchResponse
import com.github.andreyasadchy.xtra.model.helix.clip.ClipsResponse
import com.github.andreyasadchy.xtra.model.helix.emote.EmoteSetResponse
import com.github.andreyasadchy.xtra.model.helix.follows.FollowResponse
import com.github.andreyasadchy.xtra.model.helix.game.GamesResponse
import com.github.andreyasadchy.xtra.model.helix.stream.StreamsResponse
import com.github.andreyasadchy.xtra.model.helix.user.UsersResponse
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.VideosResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HelixApi {

    @GET("games")
    suspend fun getGame(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("id") id: String): GamesResponse

    @GET("games/top")
    suspend fun getTopGames(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("first") limit: Int, @Query("after") offset: String?): GamesResponse

    @GET("streams/")
    suspend fun getStream(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("user_id") channelId: String, @Query("after") offset: String?): StreamsResponse

    @GET("streams/")
    suspend fun getStreams(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("game_id") game: String?, @Query("language") languages: String?, @Query("first") limit: Int, @Query("after") offset: String?): StreamsResponse

    @GET("streams/followed")
    suspend fun getFollowedStreams(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("user_id") user_id: String?, @Query("first") limit: Int, @Query("after") offset: String?): StreamsResponse

    @GET("clips")
    suspend fun getClips(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("broadcaster_id") channel: String?, @Query("game_id") gameName: String?, @Query("started_at") started_at: String?, @Query("ended_at") ended_at: String?, @Query("first") limit: Int, @Query("after") cursor: String?): ClipsResponse

    @GET("videos")
    suspend fun getVideo(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("id") videoId: String): VideosResponse

    @GET("videos")
    suspend fun getTopVideos(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("game_id") game: String?, @Query("period") period: com.github.andreyasadchy.xtra.model.helix.video.Period?, @Query("type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("first") limit: Int, @Query("after") offset: String?): VideosResponse

    @GET("videos")
    suspend fun getChannelVideos(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("user_id") channelId: String, @Query("period") period: com.github.andreyasadchy.xtra.model.helix.video.Period?, @Query("type") broadcastType: BroadcastType?, @Query("sort") sort: Sort?, @Query("first") limit: Int, @Query("after") offset: String?): VideosResponse

    @GET("users")
    suspend fun getUserById(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("id") id: String): UsersResponse

    @GET("users")
    suspend fun getUsersByLogin(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("login") logins: String): UsersResponse

    @GET("search/categories")
    suspend fun getGames(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("query") query: String, @Query("first") limit: Int, @Query("after") offset: String?): GamesResponse

    @GET("search/channels")
    suspend fun getChannels(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("query") query: String, @Query("first") limit: Int, @Query("after") offset: String?): ChannelSearchResponse

    @GET("users/follows")
    suspend fun getUserFollows(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("to_id") userId: String, @Query("from_id") channelId: String): FollowResponse

    @GET("users/follows")
    suspend fun getFollowedChannels(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("from_id") userId: String, @Query("first") limit: Int, @Query("after") offset: String?): FollowResponse

    @GET("chat/emotes/set")
    suspend fun getEmotesFromSet(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Query("emote_set_id") setId: String?): EmoteSetResponse
}