package com.github.exact7.xtra.ui.player

import androidx.lifecycle.MutableLiveData

class PlayerHelper {

    var urls: Map<String, String>? = null
    var selectedQualityIndex = 0
    val loaded = MutableLiveData<Boolean>()
}