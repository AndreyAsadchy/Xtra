package com.github.exact7.xtra.ui.streams.followed

import androidx.fragment.app.viewModels
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel by viewModels<FollowedStreamsViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.setUser(User.get(requireContext()))
    }
}
