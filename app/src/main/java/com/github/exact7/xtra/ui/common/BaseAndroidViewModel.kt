package com.github.exact7.xtra.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import io.reactivex.disposables.CompositeDisposable

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposableDelegate = lazy { CompositeDisposable() }
    protected val compositeDisposable by compositeDisposableDelegate

    protected val _errors by lazy { MediatorLiveData<Throwable>() }
    val errors: LiveData<Throwable>
        get() = _errors

    override fun onCleared() {
        if (compositeDisposableDelegate.isInitialized()) {
            compositeDisposable.clear()
        }
        super.onCleared()
    }
}