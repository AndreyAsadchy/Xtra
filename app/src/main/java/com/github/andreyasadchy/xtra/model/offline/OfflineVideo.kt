package com.github.andreyasadchy.xtra.model.offline

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
        @ColumnInfo(name = "source_url")
        val sourceUrl: String,
        @ColumnInfo(name = "source_start_position")
        val sourceStartPosition: Long?,
        val name: String,
        @ColumnInfo(name = "channel_name")
        val channelName: String,
        @ColumnInfo(name = "channel_logo")
        val channelLogo: String,
        val thumbnail: String,
        val game: String, //TODO write a proper migration to null?
        val duration: Long,
        @ColumnInfo(name = "upload_date")
        val uploadDate: Long,
        @ColumnInfo(name = "download_date")
        val downloadDate: Long,
        @ColumnInfo(name = "last_watch_position")
        var lastWatchPosition: Long,
        var progress: Int,
        @ColumnInfo(name = "max_progress")
        val maxProgress: Int,
        var status: Int = STATUS_PENDING) : Parcelable {

    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @IgnoredOnParcel
    @ColumnInfo(name = "is_vod")
    var vod = url.endsWith(".m3u8")

    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_DOWNLOADING = 1
        const val STATUS_DOWNLOADED = 2
    }
}
