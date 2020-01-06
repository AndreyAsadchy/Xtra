package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.kraken.clip.ClipDataResponse
import com.google.gson.JsonArray
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

@JvmSuppressWildcards
interface GraphQLApi {

    @POST(".")
    fun getClipData(@Body json: JsonArray): Single<ClipDataResponse>
}