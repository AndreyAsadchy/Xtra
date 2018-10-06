package com.exact.xtra.api

import com.exact.xtra.model.chat.VideoMessagesResponse
import com.exact.xtra.model.clip.ClipsResponse
import com.exact.xtra.model.game.GamesResponse
import com.exact.xtra.model.stream.StreamsResponse
import com.exact.xtra.model.user.User
import com.exact.xtra.model.user.UserEmotesResponse
import com.exact.xtra.model.user.UsersResponse
import com.exact.xtra.model.video.VideosResponse
import com.exact.xtra.ui.clips.Period
import com.exact.xtra.ui.streams.StreamType
import com.exact.xtra.ui.videos.BroadcastType
import com.exact.xtra.ui.videos.Sort
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface KrakenApi {

    @GET("games/top")
    fun getTopGames(@Query("limit") limit: Int, @Query("offset") offset: Int): Single<GamesResponse>

    @GET("streams/")
    fun getStreams(@Query("game") game: String?, @Query("language") languages: String?, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("streams/followed")
    fun getFollowedStreams(@Header("Authorization") token: String, @Query("stream_type") streamType: StreamType?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<StreamsResponse>

    @GET("videos/{id}/comments")
    fun getVideoChatLog(@Path("id") videoId: String, @Query("content_offset_seconds") offsetSeconds: Double, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("videos/{id}/comments")
    fun getVideoChatLogAfter(@Path("id") videoId: String, @Query("cursor") cursor: String, @Query("limit") limit: Int): Single<VideoMessagesResponse>

    @GET("clips/top")
    fun getClips(@Query("channel") channelName: String?, @Query("game") gameName: String?, @Query("language") languages: String?, @Query("period") period: Period?, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("clips/followed")
    fun getFollowedClips(@Header("Authorization") token: String, @Query("trending") trending: Boolean?, @Query("limit") limit: Int, @Query("cursor") cursor: String?): Single<ClipsResponse>

    @GET("videos/top")
    fun getTopVideos(@Query("game") game: String?, @Query("period") period: com.exact.xtra.ui.videos.Period?, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("videos/followed")
    fun getFollowedVideos(@Header("Authorization") token: String, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("language") language: String?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("channels/{id}/videos")
    fun getChannelVideos(@Path("id") channelId: Any, @Query("broadcast_type") broadcastType: BroadcastType?, @Query("sort") sort: Sort?, @Query("limit") limit: Int, @Query("offset") offset: Int): Single<VideosResponse>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Single<User>

    @GET("users/{username}")
    fun getUserByLogin(@Path("username") login: String): Single<User>

    @GET("users")
    fun getUsers(@Query("username") logins: String): Single<UsersResponse>

    @GET("users/{id}/emotes")
    fun getUserEmotes(@Path("id") userId: Int): Single<UserEmotesResponse>
}
