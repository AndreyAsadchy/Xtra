package com.github.exact7.xtra.model.user

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    @SerializedName("_total")
    val total: Int,
    val users: List<User>)