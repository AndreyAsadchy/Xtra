package com.exact.twitch.api

import com.exact.twitch.model.chat.VideoMessagesResponse
import com.exact.twitch.model.clip.ClipsResponse
import com.exact.twitch.model.game.GamesResponse
import com.exact.twitch.model.stream.StreamsResponse
import com.exact.twitch.model.user.User
import com.exact.twitch.model.user.UserEmotesResponse
import com.exact.twitch.model.user.UsersResponse
import com.exact.twitch.model.video.VideosResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface KrakenApi {

    @GET("games/top")
    fun getTopGames(@Query("limit") limit: Int, @Query("offset") offset: Int): Single<GamesResponse>

    @GET("streams/")
    fun getStreams(@Query("game") game: String?, @Query("language") languages: String?, @Query("stream_type") streamType: String?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("streams/followed")
    fun getFollowedStreams(@Header("Authorization") token: String, @Query("stream_type") streamType: String?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("videos/{id}/comments")
    fun getVideoChatLog(@Path("id") videoId: String, @Query("content_offset_seconds") offsetSeconds: Double, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("videos/{id}/comments")
    fun getVideoChatLogAfter(@Path("id") videoId: String, @Query("cursor") cursor: String, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("clips/top")
    fun getClips(@Query("channel") channelName: String?, @Query("game") gameName: String?, @Query("language") languages: String?, @Query("period") period: String?, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("clips/followed")
    fun getFollowedClips(@Header("Authorization") token: String, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("videos/top")
    fun getTopVideos(@Query("game") game: String?, @Query("period") period: String?, @Query("broadcast_type") broadcastTypes: String?, @Query("language") language: String?, @Query("sort") sort: String?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("videos/followed")
    fun getFollowedVideos(@Header("Authorization") token: String, @Query("broadcast_type") broadcastTypes: String?, @Query("language") language: String?, @Query("sort") sort: String?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("channels/{id}/videos")
    fun getChannelVideos(@Path("id") channelId: Any, @Query("broadcast_type") broadcastTypes: String?, @Query("sort") sort: String?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Single<User>

    @GET("users/{login}")
    fun getUserByLogin(@Path("login") login: String): Single<User>

    @GET("users")
    fun getUsers(@Query("login") logins: String): Single<UsersResponse>

    @GET("users/{id}/emotes")
    fun getUserEmotes(@Path("id") userId: Int): Single<UserEmotesResponse>
}
