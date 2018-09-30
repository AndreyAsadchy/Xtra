package com.exact.twitch.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

open class ListViewModel<L : List<*>> : ViewModel() {

    var list: LiveData<L>? = null
        protected set
}
