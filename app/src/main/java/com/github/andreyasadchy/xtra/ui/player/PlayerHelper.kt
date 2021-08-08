package com.github.andreyasadchy.xtra.ui.player

import androidx.lifecycle.MutableLiveData

class PlayerHelper {

    var urls: Map<String, String> = emptyMap()
    val loaded = MutableLiveData<Boolean>()
}