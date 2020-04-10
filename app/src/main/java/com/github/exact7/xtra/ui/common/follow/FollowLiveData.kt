package com.github.exact7.xtra.ui.common.follow

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FollowLiveData(
        private val repository: TwitchService,
        private val user: LoggedIn,
        private val channelId: String,
        private val viewModelScope: CoroutineScope) : MutableLiveData<Boolean>()  {

    init {
        viewModelScope.launch {
            try {
                val isFollowing = repository.loadUserFollows(user.id, channelId)
                super.setValue(isFollowing)
            } catch (e: Exception) {

            }
        }
    }

    override fun setValue(value: Boolean) {
        viewModelScope.launch {
            if (value) {
                val followed = repository.followChannel(user.token, user.id, channelId)
                super.setValue(followed)
            } else {
                val unfollowed = repository.unfollowChannel(user.token, user.id, channelId)
                super.setValue(!unfollowed)
            }
        }
    }
}