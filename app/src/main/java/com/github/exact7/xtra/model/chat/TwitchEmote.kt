package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import androidx.room.Ignore
import com.github.exact7.xtra.util.C
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int,
        var end: Int) : Emote(), Parcelable {

    @Ignore
    @IgnoredOnParcel
    override val url: String = "${C.TWITCH_EMOTES_URL}$name/2.0"

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}
