package com.exact.twitch.ui.main

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(): ViewModel() {

    val isPlayerOpened = MutableLiveData<Boolean>()
    val isPlayerMaximized = MutableLiveData<Boolean>()
    val username = MutableLiveData<String>()
    val isUserLoggedIn = MediatorLiveData<Boolean>().apply {
        addSource(username) {
            postValue(it != null)
        }
    }
}