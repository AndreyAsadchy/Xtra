package com.github.exact7.xtra.ui.main

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.NotValidated
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.repository.AuthRepository
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.util.Event
import com.github.exact7.xtra.util.TwitchApiHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val authRepository: AuthRepository
): ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
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

    private val compositeDisposable = CompositeDisposable()

    fun setUser(user: User) {
        if (_user.value == null) {
            _user.value = user.let {
                if (it is NotValidated) {
                    if (!TwitchApiHelper.validated) {
                        it
                    } else {
                        LoggedIn(it)
                    }
                } else {
                    it
                }
            }
        }
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

    fun setNetworkAvailable(available: Boolean) {
        if (_isNetworkAvailable.value?.peekContent() != available) {
            _isNetworkAvailable.value = Event(available)
        }
    }

    fun validate(activity: Activity) {
        val user = user.value
        if (user is NotValidated && !TwitchApiHelper.validated) {
            authRepository.validate(user.token)
                    .subscribe({
                        TwitchApiHelper.validated = true
                        _user.value = LoggedIn(user)
                    }, {
                        with(activity) {
                            Toast.makeText(this, getString(R.string.token_expired), Toast.LENGTH_LONG).show()
                            startActivityForResult(Intent(this, LoginActivity::class.java).putExtra("expired", true), 2) //TODO if player don't start <- dont need this TODO anymore?
                        }
                    })
                    .addTo(compositeDisposable)
        }
    }
}