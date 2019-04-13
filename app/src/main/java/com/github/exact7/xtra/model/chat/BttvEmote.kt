package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote(name), Parcelable {
    val isPng: Boolean
        get() = imageType.endsWith("png")
}