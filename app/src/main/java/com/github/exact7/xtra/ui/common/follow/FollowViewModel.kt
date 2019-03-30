package com.github.exact7.xtra.ui.common.follow

import com.github.exact7.xtra.model.LoggedIn

interface FollowViewModel {
    val channelInfo: Pair<String, String>
    val follow: FollowLiveData
    fun setUser(user: LoggedIn)
}