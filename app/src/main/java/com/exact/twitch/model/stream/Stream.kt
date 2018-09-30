package com.exact.twitch.model.stream

import android.os.Parcelable
import com.exact.twitch.model.Preview
import com.exact.twitch.model.channel.Channel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Stream (
    @SerializedName("_id")
    val id: Long,
    @SerializedName("average_fps")
    val averageFps: Double,
    val channel: Channel,
    @SerializedName("created_at")
    val createdAt: String,
    val delay: Int,
    val game: String,
    @SerializedName("is_playlist")
    val playlist: Boolean,
    val preview: Preview,
    @SerializedName("video_height")
    val videoHeight: Int,
    val viewers: Int) : Parcelable
