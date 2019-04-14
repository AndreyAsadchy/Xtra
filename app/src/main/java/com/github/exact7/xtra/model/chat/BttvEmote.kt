package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote(), Parcelable {
    val isPng: Boolean
        get() = imageType.endsWith("png")

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}