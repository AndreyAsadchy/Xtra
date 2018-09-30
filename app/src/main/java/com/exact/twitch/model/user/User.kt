package com.exact.twitch.model.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("_id")
    val id: String,
    val bio: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("display_name")
    val displayName: String,
    val logo: String,
    val name: String,
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String) : Parcelable