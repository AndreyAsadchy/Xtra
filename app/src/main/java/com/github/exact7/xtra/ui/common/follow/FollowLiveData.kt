package com.github.exact7.xtra.ui.common.follow

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService

class FollowLiveData(
        private val repository: TwitchService,
        private val user: LoggedIn,
        private val channelId: String) : MutableLiveData<Boolean>()  {

    init {
        repository.loadUserFollows(user.id, channelId).observeForever { super.setValue(it) }
    }

    override fun setValue(value: Boolean?) {
        if (value == true) {
            repository.followChannel(user.token, user.id, channelId).observeForever { super.setValue(it) }
        } else {
            repository.unfollowChannel(user.token, user.id, channelId).observeForever { super.setValue(!it) }
        }
    }
}