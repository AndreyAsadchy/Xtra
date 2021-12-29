package com.github.andreyasadchy.xtra.model.helix.game

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(
        val id: String? = null,
        val name: String? = null,
        val box_art_url: String? = null,
        val viewersCount: Int? = null,
        val broadcastersCount: Int? = null) : Parcelable {

        val boxArt: String?
                get() = TwitchApiHelper.getTemplateUrl(box_art_url, "game")
}