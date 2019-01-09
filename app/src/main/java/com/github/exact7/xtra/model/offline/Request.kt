package com.github.exact7.xtra.model.offline

import com.iheartradio.m3u8.data.TrackData

sealed class Request(downloadable: Downloadable) {
    val id = System.currentTimeMillis().toInt()
    val downloadable: Downloadable = Wrapper(downloadable)
}

class ClipRequest(
        downloadable: Downloadable,
        val url: String,
        val quality: String
) : Request(downloadable)

class VideoRequest(
        downloadable: Downloadable,
        val playlistUrl: String,
        val quality: String,
        val segmentFrom: Int,
        val segmentTo: Int) : Request(downloadable) {

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