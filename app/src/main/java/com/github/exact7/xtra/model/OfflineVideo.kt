package com.github.exact7.xtra.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos")
data class OfflineVideo(
    val url: String,
    val name: String,
    val channel: String,
    val game: String,
    val length: Long,
    @ColumnInfo(name = "download_date")
    val downloadDate: String,
    @ColumnInfo(name = "upload_date")
    val uploadDate: String,
    val thumbnail: String,
    val streamerAvatar: String) : Parcelable {

    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @IgnoredOnParcel
    @ColumnInfo(name = "is_vod")
    var vod = url.endsWith(".m3u8")
}
