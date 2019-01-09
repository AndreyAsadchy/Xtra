package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.PlaylistTokenResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("channels/{channelName}/access_token")
    fun getStreamAccessToken(@Path("channelName") channel: String): Single<PlaylistTokenResponse>

    @GET("vods/{id}/access_token")
    fun getVideoAccessToken(@Path("id") id: String): Single<PlaylistTokenResponse>
}
