package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.kraken.channel.ChannelsSearchResponse
import com.github.andreyasadchy.xtra.model.kraken.clip.ClipsResponse
import com.github.andreyasadchy.xtra.model.kraken.clip.Period
import com.github.andreyasadchy.xtra.model.kraken.follows.FollowedChannelsResponse
import com.github.andreyasadchy.xtra.model.kraken.follows.Order
import com.github.andreyasadchy.xtra.model.kraken.game.GamesResponse
import com.github.andreyasadchy.xtra.model.kraken.game.GamesSearchResponse
import com.github.andreyasadchy.xtra.model.kraken.stream.StreamType
import com.github.andreyasadchy.xtra.model.kraken.stream.StreamsResponse
import com.github.andreyasadchy.xtra.model.kraken.user.User
import com.github.andreyasadchy.xtra.model.kraken.user.UserEmotesResponse
import com.github.andreyasadchy.xtra.model.kraken.user.UsersResponse
import com.github.andreyasadchy.xtra.model.kraken.video.BroadcastType
import com.github.andreyasadchy.xtra.model.kraken.video.Sort
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.model.kraken.video.VideosResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface KrakenApi {

    @GET("games/top")
    suspend fun getTopGames(@Query("limit") limit: Int, @Query("offset") offset: Int): GamesResponse

    @GET("search/games")
    suspend fun getGames(@Query("query") query: String): GamesSearchResponse

    @GET("streams/")
    suspend fun getStream(@Query("channel") channelId: String): StreamsResponse

    @GET("streams/")
    suspend fun getStreams(@Query("game") game: String?, @Query("language") languages: String?, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): StreamsResponse

    @GET("streams/followed")
    suspend fun getFollowedStreams(@Header("Authorization") token: String, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): StreamsResponse

    @GET("clips/top")
    suspend fun getClips(@Query("channel") channel: String?, @Query("game") gameName: String?, @Query("language") languages: String?, @Query("period") period: Period?, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): ClipsResponse

    @GET("clips/followed")
    suspend fun getFollowedClips(@Header("Authorization") token: String, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): ClipsResponse

    @GET("videos/{id}")
    suspend fun getVideo(@Path("id") videoId: String): Video

    @GET("videos/top")
    suspend fun getTopVideos(@Query("game") game: String?, @Query("period") period: com.github.andreyasadchy.xtra.model.kraken.video.Period?, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): VideosResponse

    @GET("videos/followed")
    suspend fun getFollowedVideos(@Header("Authorization") token: String, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): VideosResponse

    @GET("channels/{id}/videos")
    suspend fun getChannelVideos(@Path("id") channelId: String, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): VideosResponse

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): User

    @GET("users")
    suspend fun getUsersByLogin(@Query("login") logins: String): UsersResponse

    @GET("users/{id}/emotes")
    suspend fun getUserEmotes(@Header("Authorization") token: String, @Path("id") userId: String): UserEmotesResponse

    @GET("search/channels")
    suspend fun getChannels(@Query("query") query: String, @Query("limit") limit: Int, @Query("offset") offset: Int): ChannelsSearchResponse

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    suspend fun getVideoChatLog(@Path("id") videoId: String, @Query("content_offset_seconds") offsetSeconds: Double, @Query("limit") limit: Int): VideoMessagesResponse

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    suspend fun getVideoChatLogAfter(@Path("id") videoId: String, @Query("cursor") cursor: String, @Query("limit") limit: Int): VideoMessagesResponse

    @GET("users/{id}/follows/channels/{channelId}")
    suspend fun getUserFollows(@Path("id") userId: String, @Path("channelId") channelId: String): Response<ResponseBody>

    @PUT("users/{id}/follows/channels/{channelId}")
    suspend fun followChannel(@Header("Authorization") token: String, @Path("id") userId: String, @Path("channelId") channelId: String): Response<ResponseBody>

    @DELETE("users/{id}/follows/channels/{channelId}")
    suspend fun unfollowChannel(@Header("Authorization") token: String, @Path("id") userId: String, @Path("channelId") channelId: String): Response<ResponseBody>

    @GET("users/{id}/follows/channels")
    suspend fun getFollowedChannels(@Path("id") userId: String, @Query("sortby") sort: com.github.andreyasadchy.xtra.model.kraken.follows.Sort, @Query("direction") order: Order, @Query("limit") limit: Int, @Query("offset") offset: Int): FollowedChannelsResponse

    @GET("users/{channelId}/follows/games")
    suspend fun getFollowedGames(@Path("channelId") channelId: String): Response<ResponseBody>
}