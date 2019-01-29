package com.github.exact7.xtra.ui.common

import com.github.exact7.xtra.model.kraken.Channel


interface OnChannelSelectedListener {
    fun viewChannel(channel: Channel)
}