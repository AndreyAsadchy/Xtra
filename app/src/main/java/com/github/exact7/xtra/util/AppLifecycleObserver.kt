package com.github.exact7.xtra.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver : DefaultLifecycleObserver {

    private val listeners = arrayListOf<LifecycleListener>()

    override fun onStart(owner: LifecycleOwner) {
        listeners.forEach { it.onMovedToForeground() }
    }

    override fun onStop(owner: LifecycleOwner) {
        listeners.forEach { it.onMovedToBackground() }
    }

    fun addListener(listener: LifecycleListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: LifecycleListener) {
        listeners.remove(listener)
    }
}