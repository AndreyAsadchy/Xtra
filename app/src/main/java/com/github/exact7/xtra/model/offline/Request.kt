package com.github.exact7.xtra.model.offline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.iheartradio.m3u8.data.TrackData
import java.util.ArrayList

sealed class Request {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @Ignore
    lateinit var path: String
}

@Entity(tableName = "clip_requests")
class ClipRequest(
        val clip: Clip,
        val url: String,
        val quality: String
) : Request()

@Entity(tableName = "video_requests")
class VideoRequest(
        val video: Video,
        val quality: String,
        @ColumnInfo(name = "base_url")
        val baseUrl: String,
        val segments: ArrayList<Pair<String, Long>>,
        @ColumnInfo(name = "target_duration")
        val targetDuration: Int) : Request() {

    @Ignore
    var totalDuration = 0L
    @Ignore
    val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
        fun parse(trackData: TrackData) =
                trackData.uri.substring(trackData.uri.lastIndexOf('/') + 1, trackData.uri.lastIndexOf('.')).let { trackName ->
                    if (!trackName.endsWith("muted")) trackName.toInt() else trackName.substringBefore('-').toInt()
                }

        val index1 = parse(o1)
        val index2 = parse(o2)
        when {
            index1 > index2 -> 1
            index1 < index2 -> -1
            else -> 0
        }
    })
}