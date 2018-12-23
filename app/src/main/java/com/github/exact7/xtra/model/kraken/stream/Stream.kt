package com.github.exact7.xtra.model.kraken.stream

import android.os.Parcelable
import com.github.exact7.xtra.model.Preview
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Stream(
        @SerializedName("_id")
        val id: Long,
        val game: String,
        @SerializedName("broadcast_platform")
        val broadcastPlatform: String,
        @SerializedName("community_id")
        val communityId: String,
        @SerializedName("community_ids")
        val communityIds: List<String>,
        val viewers: Int,
        @SerializedName("video_height")
        val videoHeight: Int,
        @SerializedName("average_fps")
        val averageFps: Double,
        val delay: Int,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("is_playlist")
        val isPlaylist: Boolean,
        @SerializedName("stream_type")
        val streamType: StreamType,
        val preview: Preview,
        val channel: Channel) : Parcelable
