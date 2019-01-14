package com.github.exact7.xtra.model.offline

import android.net.Uri
import android.util.LongSparseArray
import com.iheartradio.m3u8.data.MediaPlaylist

sealed class Request(
        val offlineVideo: OfflineVideo,
        val url: String,
        val path: Uri) {
    var maxProgress = 0
    var progress = 0
    var canceled = false
}

class ClipRequest(
        offlineVideo: OfflineVideo,
        url: String,
        path: Uri
) : Request(offlineVideo, url, path) {
    var downloadRequestId = 0L
}

class VideoRequest(
        offlineVideo: OfflineVideo,
        val videoId: String,
        url: String,
        path: Uri,
        val segmentFrom: Int,
        val segmentTo: Int) : Request(offlineVideo, url, path) {

    init {
        maxProgress = segmentTo - segmentFrom
    }

    val downloadRequestsToSegmentsMap = LongSparseArray<Int>()
    var currentTrack = segmentFrom
    lateinit var playlist: MediaPlaylist
}