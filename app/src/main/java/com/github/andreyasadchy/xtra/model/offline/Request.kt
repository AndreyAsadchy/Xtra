package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
        tableName = "requests",
        foreignKeys = [ForeignKey(entity = OfflineVideo::class, parentColumns = arrayOf("id"), childColumns = arrayOf("offline_video_id"), onDelete = ForeignKey.CASCADE)])
data class Request(
        @PrimaryKey
        @ColumnInfo(name = "offline_video_id")
        val offlineVideoId: Int,
        val url: String,
        val path: String,
        @ColumnInfo(name = "video_id")
        val videoId: String? = null,
        @ColumnInfo(name = "segment_from")
        val segmentFrom: Int? = null,
        @ColumnInfo(name = "segment_to")
        var segmentTo: Int? = null) : Parcelable