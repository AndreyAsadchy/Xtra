package com.github.andreyasadchy.xtra.api;

import okhttp3.ResponseBody
import retrofit2.Response

import kotlin.jvm.JvmSuppressWildcards;
import retrofit2.http.GET;
import retrofit2.http.Path
import retrofit2.http.QueryMap

@JvmSuppressWildcards
interface TTVLolApi {


    @GET("playlist/{channel}.m3u8")
    suspend fun getPlaylist(@Path("channel") channel: String, @QueryMap options: Map<String, String>): Response<ResponseBody>

    @GET("ping")
    suspend fun ping(): Response<ResponseBody>

}
