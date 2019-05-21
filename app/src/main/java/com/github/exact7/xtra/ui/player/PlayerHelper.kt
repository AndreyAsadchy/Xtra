package com.github.exact7.xtra.ui.player

import androidx.lifecycle.MutableLiveData

class PlayerHelper {

    var urls: Map<String, String>? = null
    var selectedQualityIndex = -1
    val loaded = MutableLiveData<Boolean>()
}