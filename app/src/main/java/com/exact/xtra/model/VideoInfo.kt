package com.exact.xtra.model

import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist

class VideoInfo(val qualities: List<CharSequence>, val segments: MutableList<HlsMediaPlaylist.Segment>, val totalDuration: Long, val targetDuration: Long) {
    val hours = totalDuration / 3600
    val minutes = totalDuration % 3600 / 60
    val seconds = totalDuration % 60
}