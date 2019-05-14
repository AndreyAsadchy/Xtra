package com.github.exact7.xtra.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel : ViewModel() {

    private val compositeDisposableDelegate = lazy { CompositeDisposable() }
    private val compositeDisposable by compositeDisposableDelegate

    protected val _errors by lazy { MutableLiveData<Throwable>() }
    val errors: LiveData<Throwable>
        get() = _errors

    infix fun call(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        if (compositeDisposableDelegate.isInitialized()) {
            compositeDisposable.clear()
        }
        super.onCleared()
    }
}