package com.github.exact7.xtra.model.offline

import android.os.Parcelable
import androidx.databinding.ObservableInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos")
data class OfflineVideo(
        val url: String,
        val name: String,
        @ColumnInfo(name = "channel_name")
        val channelName: String,
        @ColumnInfo(name = "channel_logo")
        val channelLogo: String,
        val thumbnail: String,
        val game: String,
        val duration: Long,
        @ColumnInfo(name = "upload_date")
        val uploadDate: String,
        @ColumnInfo(name = "download_date")
        val downloadDate: String) : Parcelable {

    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @IgnoredOnParcel
    @ColumnInfo(name = "is_vod")
    var vod = url.endsWith(".m3u8")

    @IgnoredOnParcel
    @Ignore
    var downloadProgress = ObservableInt()

    @IgnoredOnParcel
    @Ignore
    var maxProgress = 0

    @IgnoredOnParcel
    val downloaded: Boolean
        get() = downloadProgress.get() == maxProgress
}
