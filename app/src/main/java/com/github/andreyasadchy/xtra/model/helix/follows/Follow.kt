package com.github.andreyasadchy.xtra.model.helix.follows

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Follow(
        val from_id: String,
        val from_login: String,
        val from_name: String,
        val to_id: String,
        val to_login: String,
        val to_name: String,
        val followed_at: String,
        var profileImageURL: String? = null) : Parcelable {

        val channelLogo: String?
                get() = TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage")
}