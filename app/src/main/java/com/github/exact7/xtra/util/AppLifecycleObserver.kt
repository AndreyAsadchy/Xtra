package com.github.exact7.xtra.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleObserver : LifecycleObserver {

    private var listener: LifecycleListener? = null //TODO if needed more make addListener

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        listener?.onMovedToForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        listener?.onMovedToBackground()
    }

    fun setLifecycleListener(listener: LifecycleListener?) {
        this.listener = listener
    }
}