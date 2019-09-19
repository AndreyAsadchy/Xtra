package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

@Parcelize
class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote(), Parcelable {

    override val isPng: Boolean
        get() = imageType.endsWith("png")

    override val url: String
        get() = "$BTTV_URL$id/2x"
}