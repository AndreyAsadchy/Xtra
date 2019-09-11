package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FfzEmote(
        override val name: String,
        override val url: String) : Emote(), Parcelable