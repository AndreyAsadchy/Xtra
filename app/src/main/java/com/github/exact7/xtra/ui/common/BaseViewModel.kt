package com.github.exact7.xtra.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val _errors by lazy { MediatorLiveData<Throwable>() }
    val errors: LiveData<Throwable>
        get() = _errors
}