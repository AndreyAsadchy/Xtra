package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BttvEmote(
        val id: String,
        val code: String,
        val imageType: String) : Parcelable {
    val isPng: Boolean
        get() = imageType.endsWith(".png")
}