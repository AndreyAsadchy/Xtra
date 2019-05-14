package com.github.exact7.xtra.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MessageClickedViewModel @Inject constructor(private val repository: TwitchService) : BaseViewModel() {

    private val user = MutableLiveData<User>()
    private var isLoading = false

    fun loadUser(channelName: String): LiveData<User> {
        if (user.value == null && !isLoading) {
            isLoading = true
            call(repository.loadUserByLogin(channelName)
                    .doOnError { isLoading = false }
                    .subscribeBy(onSuccess = user::setValue, onError = _errors::setValue))
        }
        return user
    }
}