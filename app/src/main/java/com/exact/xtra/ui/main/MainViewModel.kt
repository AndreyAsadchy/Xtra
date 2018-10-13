package com.exact.xtra.ui.main

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exact.xtra.model.User
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {

    val isPlayerOpened = MutableLiveData<Boolean>()
    val isPlayerMaximized = MutableLiveData<Boolean>()
    val user = MutableLiveData<User?>()
    val isUserLoggedIn = MediatorLiveData<Boolean>().apply {
        addSource(user) {
            postValue(it != null)
        }
    }
}