package com.github.andreyasadchy.xtra.model.helix.stream

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Stream(
        val id: String? = null,
        val user_id: String? = null,
        val user_login: String? = null,
        val user_name: String? = null,
        val game_id: String? = null,
        val game_name: String? = null,
        val type: String? = null,
        val title: String? = null,
        val viewer_count: Int? = null,
        val started_at: String? = null,
        val language: String? = null,
        val thumbnail_url: String? = null,
        var profileImageURL: String? = null) : Parcelable {

        val thumbnail: String?
                get() = TwitchApiHelper.getTemplateUrl(thumbnail_url, "video")
        val channelLogo: String?
                get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
}
