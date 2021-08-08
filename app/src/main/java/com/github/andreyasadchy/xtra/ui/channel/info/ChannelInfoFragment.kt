package com.github.andreyasadchy.xtra.ui.channel.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import kotlinx.android.synthetic.main.fragment_channel_info.*

class ChannelInfoFragment : BaseNetworkFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_channel_info, container, false)
    }

    override fun initialize() {
        text.text = "Hello ".repeat(10)
    }

    override fun onNetworkRestored() {

    }
}