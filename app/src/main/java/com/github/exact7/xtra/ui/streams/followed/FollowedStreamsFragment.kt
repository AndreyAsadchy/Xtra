package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.Observer
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override fun initialize() {
        super.initialize()
        getMainViewModel().user.observe(viewLifecycleOwner, Observer {
            viewModel.setUser(it)
        })
    }

    override fun createViewModel(): FollowedStreamsViewModel = getViewModel()
}
