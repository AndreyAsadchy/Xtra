package com.github.andreyasadchy.xtra.ui.common.follow

import com.github.andreyasadchy.xtra.model.LoggedIn

interface FollowViewModel {
    val channelInfo: Pair<String, String>
    val follow: FollowLiveData
    fun setUser(user: LoggedIn)
}