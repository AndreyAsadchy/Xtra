package com.github.exact7.xtra.ui.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.NotValidated
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.repository.AuthRepository
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.Event
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.prefs
import com.github.exact7.xtra.util.toast
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
        application: Application,
        private val repository: TwitchService,
        private val authRepository: AuthRepository,
        private val offlineRepository: OfflineRepository) : ViewModel() {

    private val _isNetworkAvailable = MutableLiveData<Event<Boolean>>()
    val isNetworkAvailable: LiveData<Event<Boolean>>
        get() = _isNetworkAvailable

    var isPlayerMaximized = false
        private set

    var isPlayerOpened = false
        private set

    init {
        offlineRepository.resumeDownloads(application, application.prefs().getString(C.DOWNLOAD_NETWORK_PREFERENCE, "3") == "2")
    }

    fun onMaximize() {
        isPlayerMaximized = true
    }

    fun onMinimize() {
        isPlayerMaximized = false
    }

    fun onPlayerStarted() {
        isPlayerOpened = true
        isPlayerMaximized = true
    }

    fun onPlayerClosed() {
        isPlayerOpened = false
        isPlayerMaximized = false
    }

    fun setNetworkAvailable(available: Boolean) {
        if (_isNetworkAvailable.value?.peekContent() != available) {
            _isNetworkAvailable.value = Event(available)
        }
    }

    fun validate(activity: Activity) {
        val user = User.get(activity)
        if (TwitchApiHelper.checkedValidation) {
            if (user is LoggedIn) {
                viewModelScope.launch {
                    try {
                        repository.loadUserEmotes(user.token, user.id)
                    } catch (e: Exception) {

                    }
                }
            }
            return
        }
        if (user is NotValidated) {
            viewModelScope.launch {
                try {
                    val response = authRepository.validate(user.token)
                    if (response?.clientId == TwitchApiHelper.CLIENT_ID) { //stop using twitch client id for everything
                        User.validated()
                        repository.loadUserEmotes(user.token, user.id)
                    } else {
                        throw IllegalStateException("401")
                    }
                } catch (e: Exception) {
                    if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                        User.set(activity, null)
                        activity.toast(R.string.token_expired)
                        activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 2)
                        activity.prefs().edit { putString(C.LAST_UPDATE_VERSION, null) }
                    }
                }
            }
        }
        TwitchApiHelper.checkedValidation = true
    }
}