package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.PlaylistTokenResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("channels/{channel}/access_token")
    suspend fun getStreamAccessToken(@Header("Client-ID") clientId: String, @Header("cookie") cookie: String, @Path("channel") channel: String, @Query("oauth_token") token: String, @Query("player_type") playerType: String): PlaylistTokenResponse

    @GET("vods/{id}/access_token")
    suspend fun getVideoAccessToken(@Header("Client-ID") clientId: String, @Path("id") id: String, @Query("oauth_token") token: String): PlaylistTokenResponse
}
