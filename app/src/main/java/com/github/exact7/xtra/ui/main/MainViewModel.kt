package com.github.exact7.xtra.ui.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.util.Event
import javax.inject.Inject

class MainViewModel @Inject constructor(application: Application): ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _playerMaximized = MutableLiveData<Boolean>()
    val playerMaximized: LiveData<Boolean>
        get() = _playerMaximized

    private val _isNetworkAvailable = MutableLiveData<Event<Boolean>>()
    val isNetworkAvailable: LiveData<Event<Boolean>>
        get() = _isNetworkAvailable

    val isPlayerMaximized: Boolean
        get() = _playerMaximized.value ?: false

    var isPlayerOpened = false
        private set
    private val cm: ConnectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            _isNetworkAvailable.postValue(Event(true))
        }

        override fun onLost(network: Network?) {
            super.onLost(network)
            _isNetworkAvailable.postValue(Event(false))
        }
    }

    init {
        cm.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
    }

    fun setUser(user: User?) {
        _user.value = user
    }

    fun onMaximize() {
        _playerMaximized.value = true
    }

    fun onMinimize() {
        if (_playerMaximized.value != false)
            _playerMaximized.value = false
    }

    fun onPlayerStarted() {
        isPlayerOpened = true
        _playerMaximized.value = true
    }

    fun onPlayerClosed() {
        isPlayerOpened = false
        _playerMaximized.value = false
    }

    override fun onCleared() {
        super.onCleared()
        cm.unregisterNetworkCallback(networkCallback)
    }
}