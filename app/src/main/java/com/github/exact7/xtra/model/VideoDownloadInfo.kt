package com.github.exact7.xtra.model

import android.os.Parcelable
import com.github.exact7.xtra.model.kraken.video.Video
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoDownloadInfo(
        val video: Video,
        val qualities: Map<String, String>,
        val relativeStartTimes: List<Long>,
        val durations: List<Long>,
        val totalDuration: Long,
        val targetDuration: Long,
        val currentPosition: Long) : Parcelable