package com.github.andreyasadchy.xtra.model.helix.channel

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Channel(
        val id: String? = null,
        val broadcaster_login: String? = null,
        val display_name: String? = null,
        val game_id: String? = null,
        val game_name: String? = null,
        val is_live: Boolean = false,
        val title: String? = null,
        val started_at: String? = null,
        val broadcaster_language: String? = null,
        val thumbnail_url: String? = null,
        var profileImageURL: String? = null) : Parcelable {

        val channelLogo: String?
                get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
}