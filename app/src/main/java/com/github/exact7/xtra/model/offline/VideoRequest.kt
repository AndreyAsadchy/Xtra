package com.github.exact7.xtra.model.offline

import android.net.Uri
import android.util.LongSparseArray
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.kraken.video.Video
import com.iheartradio.m3u8.data.TrackData
import java.util.ArrayList

@Entity(tableName = "requests")
class VideoRequest(
        @PrimaryKey
        val id: Int,
        val video: Video,
        val quality: String,
        @ColumnInfo (name = "base_url")
        val baseUrl: String,
        val segments: ArrayList<Pair<String, Long>>,
        @ColumnInfo (name = "target_duration")
        val targetDuration: Int) {

    @Ignore val maxProgress = segments.size
    @Ignore var currentProgress = 0
    @Ignore var totalDuration = 0L
    @Ignore lateinit var directoryUri: Uri
    @Ignore lateinit var directoryPath: String
    @Ignore val downloadRequestToSegmentMap = LongSparseArray<Int>()
    @Ignore val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
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