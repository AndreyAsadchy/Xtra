package com.github.andreyasadchy.xtra.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UsherApi {

    @GET("api/channel/hls/{channel}.m3u8")
    suspend fun getStreamPlaylist(@Path("channel") channel: String, @QueryMap options: Map<String, String>): Response<ResponseBody>

    @GET("vod/{id}.m3u8")
    suspend fun getVideoPlaylist(@Path("id") id: String, @QueryMap options: Map<String, String>): Response<ResponseBody>
}
