package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.PlaylistTokenResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("channels/{channel}/access_token")
    fun getStreamAccessToken(@Header("Client-ID") clientId: String, @Path("channel") channel: String, @Query("oauth_token") token: String): Single<PlaylistTokenResponse>

    @GET("vods/{id}/access_token")
    fun getVideoAccessToken(@Header("Client-ID") clientId: String, @Path("id") id: String, @Query("oauth_token") token: String): Single<PlaylistTokenResponse>
}
