package com.github.andreyasadchy.xtra.model.kraken.user

import android.os.Parcelable
import com.github.andreyasadchy.xtra.model.kraken.Channel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("_id")
    override val id: String,
    val bio: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("display_name")
    override val displayName: String,
    override val logo: String,
    override val name: String,
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String) : Parcelable, Channel