package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.gql.clip.ClipDataResponse
import com.google.gson.JsonArray
import retrofit2.http.Body
import retrofit2.http.POST

@JvmSuppressWildcards
interface GraphQLApi {

    @POST(".")
    suspend fun getClipData(@Body json: JsonArray): ClipDataResponse
}