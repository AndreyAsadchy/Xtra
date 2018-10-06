package com.exact.xtra.model.id

import com.google.gson.annotations.SerializedName

class ValidationResponse(
        @SerializedName("client_id")
        val clientId: String,
        @SerializedName("login")
        val username: String,
        val scopes: List<String>,
        @SerializedName("user_id")
        val userId: String)

