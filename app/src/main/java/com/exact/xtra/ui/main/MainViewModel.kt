package com.exact.xtra.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exact.xtra.model.User
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {

    val user = MutableLiveData<User?>()

    private val _isPlayerOpened = MutableLiveData<Boolean>()
    private val _isPlayerMaximized = MutableLiveData<Boolean>()
    var isPlayerOpened
        get() = _isPlayerOpened.value ?: false
        set(value) {
            _isPlayerOpened.value = value
        }
    var isPlayerMaximized: Boolean
        get() = _isPlayerMaximized.value ?: false
        set(value) {
            _isPlayerMaximized.value = value
        }

    private val playerStatus = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        var lastA: Boolean? = null
        var lastB: Boolean? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (localLastA != null && localLastB != null)
                this.value = Pair(localLastA, localLastB)
        }

        addSource(_isPlayerOpened) {
            lastA = it
            update()
        }
        addSource(_isPlayerMaximized) {
            lastB = it
            update()
        }
    }

    /**
     * isOpened and isMaximized
     */
    fun getPlayerStatus(): LiveData<Pair<Boolean, Boolean>> {
        return playerStatus
    }
}