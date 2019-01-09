package com.github.exact7.xtra.model.kraken.video

import android.os.Parcelable
import com.github.exact7.xtra.model.Preview
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.model.offline.Downloadable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Video(
        val title: String,
        val description: String?,
        @SerializedName("description_html")
        val descriptionHtml: String?,
        @SerializedName("broadcast_id")
        val broadcastId: Long,
        @SerializedName("broadcast_type")
        val broadcastType: String,
        val status: String,
        @SerializedName("tag_list")
        val tagList: String,
        val views: Int,
        val url: String,
        val language: String,
        @SerializedName("created_at")
        val createdAt: String,
        val viewable: String,
        @SerializedName("viewable_at")
        val viewableAt: String?,
        @SerializedName("published_at")
        val publishedAt: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("recorded_at")
        val recordedAt: String,
        override val game: String,
        val communities: List<String>?,
        val length: Int,
        val preview: Preview,
        @SerializedName("animated_preview_url")
        val animatedPreviewUrl: String,
        val thumbnails: Thumbnails,
        @SerializedName("seek_previews_url")
        val seekPreviewsUrl: String,
        val restriction: String,
        val channel: Channel,
        @SerializedName("increment_view_count_url")
        val incrementViewCountUrl: String,
        @SerializedName("muted_segments")
        val mutedSegments: List<MutedSegment>?) : Parcelable, Downloadable {

    override val channelLogo: String
        get() = channel.logo
    override val channelName: String
        get() = channel.name
    override val thumbnail: String
        get() = preview.medium
    override val uploadDate: String
        get() = createdAt

    @Parcelize
    data class MutedSegment(
            val duration: Int,
            val offset: Int) : Parcelable

    @Parcelize
    data class Thumbnails(
            val small: List<Thumbnail>,
            val medium: List<Thumbnail>,
            val large: List<Thumbnail>,
            val template: List<Thumbnail>) : Parcelable {

        @Parcelize
        data class Thumbnail(
                val type: String,
                val url: String) : Parcelable
    }
}