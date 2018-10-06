package com.exact.xtra.ui.streams

import android.os.Bundle
import android.view.View
import com.exact.xtra.model.User
import com.exact.xtra.util.C

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
