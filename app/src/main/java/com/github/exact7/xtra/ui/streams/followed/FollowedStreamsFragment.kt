package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment

class FollowedStreamsFragment : BaseStreamsFragment() {

    override lateinit var viewModel: FollowedStreamsViewModel

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FollowedStreamsViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        val mainViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.user.observe(viewLifecycleOwner, Observer {
            viewModel.setUser(it!!)
        })
    }
}
