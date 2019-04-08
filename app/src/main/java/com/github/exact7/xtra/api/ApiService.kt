package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.PlaylistTokenResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiService {

    @GET("channels/{channel}/access_token")
    fun getStreamAccessToken(@Path("channel") channel: String): Single<PlaylistTokenResponse>

    @GET("vods/{id}/access_token")
    fun getVideoAccessToken(@Header("Authorization") token: String?, @Path("id") id: String): Single<PlaylistTokenResponse>
}
