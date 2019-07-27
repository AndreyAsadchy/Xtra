package com.github.exact7.xtra.ui.player

import androidx.lifecycle.MutableLiveData

class PlayerHelper {

    var urls: Map<String, String> = emptyMap()
    var qualityIndex = 0
    val loaded = MutableLiveData<Boolean>()
}