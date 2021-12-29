package com.github.andreyasadchy.xtra.model.helix.user

import android.os.Parcelable
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String? = null,
    val login: String? = null,
    val display_name: String? = null,
    val type: String? = null,
    val broadcaster_type: String? = null,
    val description: String? = null,
    val profile_image_url: String? = null,
    val offline_image_url: String? = null,
    val view_count: Int? = null,
    val created_at: String? = null) : Parcelable {

    val channelLogo: String?
        get() = TwitchApiHelper.getTemplateUrl(profile_image_url, "profileimage")
}