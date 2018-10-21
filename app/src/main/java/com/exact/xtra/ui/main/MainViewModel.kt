package com.exact.xtra.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exact.xtra.model.User
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {

    val user = MutableLiveData<User?>()
    val isPlayerOpened = MutableLiveData<Boolean>()
    val isPlayerMaximized = MutableLiveData<Boolean>()
}