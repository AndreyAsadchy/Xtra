package com.exact.twitch.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos")
data class OfflineVideo(
    val url: String,
    val name: String,
    val channel: String,
    val game: String,
    val length: Long,
    @ColumnInfo(name = "upload_date")
    val uploadDate: String,
    @ColumnInfo(name = "download_date")
    val downloadDate: String,
    val thumbnail: String,
    val streamerAvatar: String) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
