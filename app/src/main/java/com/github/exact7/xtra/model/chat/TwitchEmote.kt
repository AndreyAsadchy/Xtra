package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import com.github.exact7.xtra.util.C
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int,
        var end: Int) : Emote(), Parcelable {

    override val url: String
        get() = "${C.TWITCH_EMOTES_URL}$name/2.0"
}
