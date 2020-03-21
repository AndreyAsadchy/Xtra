package com.github.exact7.xtra.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageClickedViewModel @Inject constructor(private val repository: TwitchService) : BaseViewModel() {

    private val user = MutableLiveData<User>()
    private var isLoading = false

    fun loadUser(channelName: String): LiveData<User> {
        if (user.value == null && !isLoading) {
            isLoading = true
            viewModelScope.launch {
                try {
                    val u = repository.loadUserByLogin(channelName)
                    user.postValue(u)
                } catch (e: Exception) {
                    _errors.postValue(e)
                } finally {
                    isLoading = false
                }
            }
        }
        return user
    }
}