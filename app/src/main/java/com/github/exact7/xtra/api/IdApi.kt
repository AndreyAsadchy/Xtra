package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.id.ValidationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface IdApi {

    @GET("validate")
    suspend fun validateToken(@Header("Authorization") token: String): ValidationResponse?

    @POST("revoke")
    suspend fun revokeToken(@Query("client_id") clientId: String, @Query("token") token: String)
}