package com.github.exact7.xtra.ui.common

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService

class FollowLiveData(private val repository: TwitchService) : MutableLiveData<Boolean>()  {

    private lateinit var user: LoggedIn
    private lateinit var channelId: String

    fun initialize(user: LoggedIn, channelId: String) {
        if ((!this::user.isInitialized || this.user != user) || (!this::channelId.isInitialized || this.channelId != channelId)) {
            this.user = user
            this.channelId = channelId
            repository.loadUserFollows(user.id, channelId).observeForever { super.setValue(it) }
        }
    }

    override fun setValue(value: Boolean?) {
        if (value == true) {
            repository.followChannel(user.token, user.id, channelId).observeForever { super.setValue(it) }
        } else {
            repository.unfollowChannel(user.token, user.id, channelId).observeForever { super.setValue(!it) }
        }
    }
}