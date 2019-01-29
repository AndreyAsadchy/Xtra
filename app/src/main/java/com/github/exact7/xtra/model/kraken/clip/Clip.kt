package com.github.exact7.xtra.model.kraken.clip

import android.os.Parcelable
import com.github.exact7.xtra.model.offline.Downloadable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Clip(
        val slug: String,
        @SerializedName("tracking_id")
        val trackingId: String,
        val url: String,
        @SerializedName("embed_url")
        val embedUrl: String,
        @SerializedName("embed_html")
        val embedHtml: String,
        val broadcaster: Channel,
        val curator: Channel,
        @SerializedName("broadcast_id")
        val broadcastId: String,
        val vod: Vod?,
        override val game: String,
        val language: String,
        override val title: String,
        val views: Int,
        var duration: Double,
        @SerializedName("created_at")
        val createdAt: String,
        val thumbnails: Thumbnails) : Parcelable, Downloadable {

    override val id: String
        get() = slug
    override val thumbnail: String
        get() = thumbnails.medium
    override val channelName: String
        get() = broadcaster.name
    override val channelLogo: String
        get() = broadcaster.logo
    override val uploadDate: String
        get() = createdAt

    @Parcelize
    data class Channel(
            override val id: String,
            override val name: String,
            @SerializedName("display_name")
            override val displayName: String,
            @SerializedName("channel_url")
            val channelUrl: String,
            override val logo: String) : Parcelable, com.github.exact7.xtra.model.kraken.Channel

    @Parcelize
    data class Thumbnails(
            val medium: String,
            val small: String,
            val tiny: String) : Parcelable

    @Parcelize
    data class Vod(
            val id: String,
            val url: String) : Parcelable
}
