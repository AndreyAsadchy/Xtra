package com.exact.xtra.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exact.xtra.model.User
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {

    val user = MutableLiveData<User?>()

    private val _playerMaximized = MutableLiveData<Boolean>()
    val isPlayerMaximized: Boolean
        get() = _playerMaximized.value ?: false
    var isPlayerOpened = false
        private set
    var hasValidated = false

    fun playerMaximized(): LiveData<Boolean> {
        return _playerMaximized
    }

    fun onMaximize() {
        _playerMaximized.value = true
    }

    fun onMinimize() {
        if (_playerMaximized.value != false)
            _playerMaximized.value = false
    }

    fun onPlayerStarted() {
        _playerMaximized.value = true
        isPlayerOpened = true
    }

    fun onPlayerClosed() {
        _playerMaximized.value = false
        isPlayerOpened = false
    }
}