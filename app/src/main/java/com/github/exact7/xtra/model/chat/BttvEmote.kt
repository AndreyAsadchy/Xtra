package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

@Parcelize
data class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote(), Parcelable {
    val isPng: Boolean
        get() = imageType.endsWith("png")

    @IgnoredOnParcel
    override val url: String = "$BTTV_URL$id/2x"

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}