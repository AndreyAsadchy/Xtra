package com.exact.twitch.model.channel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Channel(
        val mature: Boolean,
        val status: String,
        @SerializedName("broadcaster_language")
        val broadcasterLanguage: String,
        @SerializedName("display_name")
        val displayName: String,
        val game: String,
        val language: String,
        @SerializedName("_id")
        val id: String,
        val name: String,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        val partner: Boolean,
        val logo: String,
        @SerializedName("video_banner")
        val videoBanner: String?,
        @SerializedName("profile_banner")
        val profileBanner: String?,
        @SerializedName("profile_banner_background_color")
        val profileBannerBackgroundColor: String?,
        val url: String,
        val views: Int,
        val followers: Int,
        @SerializedName("broadcaster_type")
        val broadcasterType: String,
        val description: String,
        @SerializedName("private_video")
        val privateVideo: Boolean,
        @SerializedName("privacy_options_enabled")
        val privacyOptionsEnabled: Boolean) : Parcelable