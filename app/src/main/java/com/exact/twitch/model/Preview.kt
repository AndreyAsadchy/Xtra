package com.exact.twitch.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Preview(
    val large: String,
    val medium: String,
    val small: String,
    val template: String) : Parcelable