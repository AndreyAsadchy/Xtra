package com.github.exact7.xtra.model.kraken.follows

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class Follow(
    val channel: Channel,
    @SerializedName("created_at")
    val createdAt: String,
    val notifications: Boolean) {

    @Parcelize
    data class Channel(
        val background: String?,
        val banner: String?,
        @SerializedName("broadcaster_language")
        val broadcasterLanguage: String,
        @SerializedName("created_at")
        val createdAt: String,
        val delay: Int?,
        @SerializedName("display_name")
        override val displayName: String,
        val followers: Int,
        val game: String?,
        @SerializedName("_id")
        override val id: String,
        val language: String,
        override val logo: String,
        val mature: Boolean,
        override val name: String,
        val partner: Boolean,
        @SerializedName("profile_banner")
        val profileBanner: String?,
        @SerializedName("profile_banner_background_color")
        val profileBannerBackgroundColor: String?,
        val status: String?,
        @SerializedName("updated_at")
        val updatedAt: String,
        val url: String,
        @SerializedName("video_banner")
        val videoBanner: String?,
        val views: Int) : com.github.exact7.xtra.model.kraken.Channel, Parcelable
}