package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FfzEmote(
        override val name: String,
        override val url: String) : Emote(), Parcelable {

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}