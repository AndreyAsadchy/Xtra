package com.github.exact7.xtra.model.offline

import com.iheartradio.m3u8.data.MediaPlaylist

sealed class Request(
        val offlineVideoId: Int,
        val url: String,
        val path: String) {
    val id = System.currentTimeMillis().toInt()
}

class ClipRequest(
        offlineVideoId: Int,
        url: String,
        path: String
) : Request(offlineVideoId, url, path)

class VideoRequest(
        offlineVideoId: Int,
        val videoId: String,
        url: String,
        path: String,
        val segmentFrom: Int,
        val segmentTo: Int) : Request(offlineVideoId, url, path) {

    var progress = 0
    val maxProgress = segmentTo - segmentFrom
    lateinit var playlist: MediaPlaylist
}