package com.github.exact7.xtra.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UsherApi {

    @GET("api/channelName/hls/{channelName}")
    fun getStreamPlaylist(@Path("channelName") channel: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>

    @GET("vod/{id}")
    fun getVideoPlaylist(@Path("id") id: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>
}
