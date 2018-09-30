package com.exact.twitch.model.id

import com.google.gson.annotations.SerializedName

class ValidationResponse(
    @SerializedName("client_id")
    val clientId: String,
    val login: String,
    val scopes: List<String>,
    @SerializedName("user_id")
    val userId: String)

