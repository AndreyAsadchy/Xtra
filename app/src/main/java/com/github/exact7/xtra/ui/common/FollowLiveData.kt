package com.github.exact7.xtra.ui.common

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class FollowLiveData(
        private val repository: TwitchService,
        private val compositeDisposable: CompositeDisposable) : MutableLiveData<Boolean>()  {

    private lateinit var user: LoggedIn
    private lateinit var channelId: String

    fun initialize(user: LoggedIn, channelId: String) {
        if (this.user != user || this.channelId != channelId) {
            this.user = user
            this.channelId = channelId
            repository.loadUserFollows(user.id, channelId)
                    .subscribe { f -> value = f }
                    .addTo(compositeDisposable)
        }
    }

    override fun setValue(value: Boolean?) {
        if (value == true) {
            repository.followChannel(user.token, user.id, channelId)
                    .subscribe { f -> super.setValue(f) }
                    .addTo(compositeDisposable)
        } else {
            repository.unfollowChannel(user.token, user.id, channelId)
                    .subscribe { f -> super.setValue(!f) }
                    .addTo(compositeDisposable)
        }
    }
}