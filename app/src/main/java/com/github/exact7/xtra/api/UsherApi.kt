package com.github.exact7.xtra.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UsherApi {

    @GET("api/channel/hls/{channel}.m3u8")
    fun getStreamPlaylist(@Path("channel") channel: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>

    @GET("vod/{id}.m3u8")
    fun getVideoPlaylist(@Path("id") id: String, @QueryMap options: Map<String, String>): Single<Response<ResponseBody>>
}
