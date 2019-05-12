package com.github.exact7.xtra.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.repository.TwitchService
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MessageClickedViewModel @Inject constructor(private val repository: TwitchService) : ViewModel() {

    private val user = MutableLiveData<User>()
    private var disposable: Disposable? = null

    fun loadUser(channelName: String): LiveData<User> {
        if (user.value == null && disposable == null) {
            disposable = repository.loadUserByLogin(channelName)
                    .subscribeBy(onSuccess = user::setValue, onError = { disposable = null }) //TODO create a base view model with composite disposable and error live data
        }
        return user
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}