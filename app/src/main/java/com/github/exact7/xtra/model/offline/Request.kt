package com.github.exact7.xtra.model.offline

import android.net.Uri
import android.util.LongSparseArray
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.iheartradio.m3u8.data.TrackData
import java.util.ArrayList

sealed class Request {
    @PrimaryKey(autoGenerate = true)
    val id = 0
    @Ignore
    var canceled = false
}

class ClipRequest(
        val clip: Clip,
        val quality: String,
        val url: String
) : Request() {
    lateinit var path: String
    var downloadRequestId = 0L
}

class VideoRequest(
        val video: Video,
        val quality: String,
        val baseUrl: String,
        val segments: ArrayList<Pair<String, Long>>,
        val targetDuration: Int) : Request() {

    val maxProgress = segments.size
    var currentProgress = 0
    var totalDuration = 0L
    lateinit var directoryUri: Uri
    lateinit var directoryPath: String
    val downloadRequestToSegmentMap = LongSparseArray<Int>()
    var deleted = false
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