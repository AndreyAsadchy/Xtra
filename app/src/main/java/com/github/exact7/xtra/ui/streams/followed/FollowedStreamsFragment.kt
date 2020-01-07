package com.github.exact7.xtra.ui.streams.followed

import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override fun initialize() {
        super.initialize()
        viewModel.setUser(User.get(requireContext()))
    }

    override fun createViewModel(): FollowedStreamsViewModel = getViewModel()
}
