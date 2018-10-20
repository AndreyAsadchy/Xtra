package com.exact.xtra.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoInfo(
        val qualities: List<CharSequence>,
        val relativeStartTimes: List<Long>,
        val totalDuration: Long,
        val targetDuration: Long,
        val currentPosition: Long) : Parcelable