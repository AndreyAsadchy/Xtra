package com.exact.xtra.api

import com.exact.xtra.model.id.ValidationResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface IdApi {

    @GET("validate")
    fun validateToken(@Header("Authorization") token: String): Single<ValidationResponse>

    @POST("revoke")
    fun revokeToken(@Query("client_id") clientId: String, @Query("token") token: String): Single<ResponseBody>
}
