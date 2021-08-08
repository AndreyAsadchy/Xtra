package com.github.andreyasadchy.xtra.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    protected val _errors by lazy { MediatorLiveData<Throwable>() }
    val errors: LiveData<Throwable>
        get() = _errors
}