package com.github.exact7.xtra.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UsherApi {

    @GET("api/channel/hls/{channel}")
    fun getStreamPlaylist(@Path("channel") channel: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>

    @GET("vod/{id}")
    fun getVideoPlaylist(@Header("Authorization") token: String?, @Path("id") id: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>
}
