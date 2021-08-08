package com.github.andreyasadchy.xtra.util

interface LifecycleListener {
    fun onMovedToForeground()
    fun onMovedToBackground()
}