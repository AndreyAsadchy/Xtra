package com.exact.twitch.ui.streams

import android.os.Bundle
import android.view.View
import com.exact.twitch.util.TwitchApiHelper

class FollowedStreamsFragment : BaseStreamsFragment() {

    private var userToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userToken = TwitchApiHelper.getUserToken(requireActivity())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (userToken == null) {
            defaultOnActivityCreated(savedInstanceState) //TODO maybe remove this and decide to open login prompt from mainactivity navigate
        } else {
            super.onActivityCreated(savedInstanceState)
        }
    }

    override fun loadData(override: Boolean) {
        if (userToken != null) {
            viewModel.loadFollowedStreams(userToken as String) //TODO unauthorized
        }
    }
}
