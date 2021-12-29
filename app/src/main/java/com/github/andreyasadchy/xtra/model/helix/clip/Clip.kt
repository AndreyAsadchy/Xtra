package com.github.andreyasadchy.xtra.model.helix.clip

import android.os.Parcelable
import com.github.andreyasadchy.xtra.model.offline.Downloadable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Clip(
        override val id: String,
        val url: String? = null,
        val embed_url: String? = null,
        val broadcaster_id: String? = null,
        val broadcaster_name: String? = null,
        val creator_id: String? = null,
        val creator_name: String? = null,
        val video_id: String? = null,
        val game_id: String? = null,
        override val title: String? = null,
        val view_count: Int? = null,
        val created_at: String? = null,
        val thumbnail_url: String? = null,
        val duration: Double? = null,

        val videoOffsetSeconds: Int? = null,
        var game_name: String? = null,
        var broadcaster_login: String? = null,
        var profileImageURL: String? = null) : Parcelable, Downloadable {

        override val thumbnail: String?
                get() = TwitchApiHelper.getTemplateUrl(thumbnail_url, "clip")
        override val channelId: String?
                get() = broadcaster_id
        override val channelName: String?
                get() = broadcaster_name
        override val channelLogo: String?
                get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
        override val gameId: String?
                get() = game_id
        override val gameName: String?
                get() = game_name
        override val uploadDate: String?
                get() = created_at
        override val videoType: String?
                get() = null
}
