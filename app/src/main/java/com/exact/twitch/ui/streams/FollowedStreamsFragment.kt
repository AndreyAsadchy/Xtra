package com.exact.twitch.ui.streams

import android.os.Bundle
import android.view.View
import com.exact.twitch.model.User
import com.exact.twitch.util.C
import com.exact.twitch.util.TwitchApiHelper

class FollowedStreamsFragment : BaseStreamsFragment() {

    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments!!.getParcelable(C.USER)!!
    }

    override fun loadData(override: Boolean) {
        viewModel.loadFollowedStreams(user.token)
    }
}
