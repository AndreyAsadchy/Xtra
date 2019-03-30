package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.util.C

class FollowedStreamsFragment : BaseStreamsFragment() {

    override lateinit var viewModel: FollowedStreamsViewModel

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FollowedStreamsViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        viewModel.setUser(requireArguments().getParcelable(C.USER)!!)
    }
}
