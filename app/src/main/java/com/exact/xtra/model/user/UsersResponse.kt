package com.exact.xtra.model.user

import com.google.gson.annotations.SerializedName

class UsersResponse(
    @SerializedName("_total")
    val total: Int,
    val users: List<User>)