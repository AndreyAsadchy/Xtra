package com.exact.twitch.api

import com.exact.twitch.model.PlaylistTokenResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("channels/{channel}/access_token")
    fun getStreamAccessToken(@Path("channel") channel: String): Single<PlaylistTokenResponse>

    @GET("vods/{id}/access_token")
    fun getVideoAccessToken(@Path("id") id: String): Single<PlaylistTokenResponse>
}
