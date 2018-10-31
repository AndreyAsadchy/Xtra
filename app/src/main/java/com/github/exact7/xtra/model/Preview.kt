package com.github.exact7.xtra.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Preview(
    val large: String,
    val medium: String,
    val small: String,
    val template: String) : Parcelable