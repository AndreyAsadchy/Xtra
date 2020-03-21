package com.github.exact7.xtra.ui.common.follow

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService

class FollowLiveData(
        private val repository: TwitchService,
        private val user: LoggedIn,
        private val channelId: String) : MutableLiveData<Boolean>()  { //TODO

//    private val disposable = repository.loadUserFollows(user.id, channelId)
//            .subscribeBy(onSuccess = { super.setValue(it) })


//    @SuppressLint("CheckResult")
//    override fun setValue(value: Boolean) {
//        if (value) {
//            repository.followChannel(user.token, user.id, channelId)
//                    .subscribeBy(onSuccess = { super.setValue(it) })
//        } else {
//            repository.unfollowChannel(user.token, user.id, channelId)
//                    .subscribeBy(onSuccess = { super.setValue(!it) })
//        }
//    }

//    override fun onInactive() {
//        super.onInactive()
//        disposable.dispose()
//    }
}