package com.github.exact7.xtra.util

import android.view.MotionEvent
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Single

fun <T> Single<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this.toFlowable())

fun MotionEvent.isClick(outDownLocation: FloatArray): Boolean { //todo move to view package
    return when (actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            outDownLocation[0] = x
            outDownLocation[1] = y
            false
        }
        MotionEvent.ACTION_UP -> outDownLocation[0] == x && outDownLocation[1] == y && eventTime - downTime <= 500
        else -> false
    }
}
