package com.exact.xtra.model.video

import android.os.Parcelable
import com.exact.xtra.model.Preview
import com.exact.xtra.model.channel.Channel
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
        val game: String,
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
        val mutedSegments: List<MutedSegment>) : Parcelable {

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