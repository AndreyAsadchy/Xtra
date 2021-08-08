package com.github.andreyasadchy.xtra.ui.common

import com.github.andreyasadchy.xtra.model.kraken.Channel


interface OnChannelSelectedListener {
    fun viewChannel(channel: Channel)
}