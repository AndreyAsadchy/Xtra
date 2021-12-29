package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.gql.channel.ChannelClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.StreamDataResponse
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST

@JvmSuppressWildcards
interface GraphQLApi {

    @POST(".")
    suspend fun getStreamAccessToken(@Header("Client-ID") clientId: String?, @HeaderMap headers: Map<String, String>, @Body json: JsonArray): StreamPlaylistTokenResponse

    @POST(".")
    suspend fun getVideoAccessToken(@Header("Client-ID") clientId: String?, @HeaderMap headers: Map<String, String>, @Body json: JsonArray): VideoPlaylistTokenResponse

    @POST(".")
    suspend fun getClipData(@Header("Client-ID") clientId: String?, @Body json: JsonArray): ClipDataResponse

    @POST(".")
    suspend fun getTopGames(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameDataResponse

    @POST(".")
    suspend fun getTopStreams(@Header("Client-ID") clientId: String?, @Body json: JsonObject): StreamDataResponse

    @POST(".")
    suspend fun getGameStreams(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameStreamsDataResponse

    @POST(".")
    suspend fun getGameVideos(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameVideosDataResponse

    @POST(".")
    suspend fun getGameClips(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameClipsDataResponse

    @POST(".")
    suspend fun getChannelVideos(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelVideosDataResponse

    @POST(".")
    suspend fun getChannelClips(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelClipsDataResponse

    @POST(".")
    suspend fun getSearchChannels(@Header("Client-ID") clientId: String?, @Body json: JsonObject): SearchChannelDataResponse

    @POST(".")
    suspend fun getSearchGames(@Header("Client-ID") clientId: String?, @Body json: JsonObject): SearchGameDataResponse

    @POST(".")
    suspend fun getChannelPanel(@Body json: JsonArray): Response<ResponseBody>
}