package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.chat.VideoMessagesResponse
import com.github.exact7.xtra.model.kraken.channel.ChannelsSearchResponse
import com.github.exact7.xtra.model.kraken.clip.ClipsResponse
import com.github.exact7.xtra.model.kraken.clip.Period
import com.github.exact7.xtra.model.kraken.game.GamesResponse
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.model.kraken.stream.StreamsResponse
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.model.kraken.user.UserEmotesResponse
import com.github.exact7.xtra.model.kraken.user.UsersResponse
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.VideosResponse
import io.reactivex.Single
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
    fun getTopGames(@Query("limit") limit: Int, @Query("offset") offset: Int): Single<GamesResponse>

    @GET("streams/")
    fun getStream(@Query("channel") channelId: String): Single<StreamsResponse>

    @GET("streams/")
    fun getStreams(@Query("game") game: String?, @Query("language") languages: String?, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("streams/followed")
    fun getFollowedStreams(@Header("Authorization") token: String, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("clips/top")
    fun getClips(@Query("channel") channel: String?, @Query("game") gameName: String?, @Query("language") languages: String?, @Query("period") period: Period?, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("clips/followed")
    fun getFollowedClips(@Header("Authorization") token: String, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("videos/top")
    fun getTopVideos(@Query("game") game: String?, @Query("period") period: com.github.exact7.xtra.model.kraken.video.Period?, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("videos/followed")
    fun getFollowedVideos(@Header("Authorization") token: String, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("channels/{id}/videos")
    fun getChannelVideos(@Path("id") channelId: String, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Single<User>

    @GET("users")
    fun getUsersByLogin(@Query("login") login: String): Single<UsersResponse>

    @GET("users/{id}/emotes")
    fun getUserEmotes(@Header("Authorization") token: String, @Path("id") userId: String): Single<UserEmotesResponse>

    @GET("search/channels")
    fun getChannels(@Query("query") query: String, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<ChannelsSearchResponse>

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    fun getVideoChatLog(@Path("id") videoId: String, @Query("content_offset_seconds") offsetSeconds: Double, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    fun getVideoChatLogAfter(@Path("id") videoId: String, @Query("cursor") cursor: String, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("users/{id}/follows/channels/{channelId}")
    fun getUserFollows(@Path("id") userId: String, @Path("channelId") channelId: String): Single<Response<ResponseBody>>

    @PUT("users/{id}/follows/channels/{channelId}")
    fun followChannel(@Header("Authorization") token: String, @Path("id") userId: String, @Path("channelId") channelId: String): Single<Response<ResponseBody>>

    @DELETE("users/{id}/follows/channels/{channelId}")
    fun unfollowChannel(@Header("Authorization") token: String, @Path("id") userId: String, @Path("channelId") channelId: String): Single<Response<ResponseBody>>
}