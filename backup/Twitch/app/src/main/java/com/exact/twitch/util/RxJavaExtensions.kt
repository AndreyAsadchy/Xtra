package com.exact.twitch.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.toLiveData(): LiveData<T> =
        LiveDataReactiveStreams.fromPublisher(this.toFlowable())