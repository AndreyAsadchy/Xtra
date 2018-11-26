package com.github.exact7.xtra.ui.streams.followed

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.ui.streams.StreamsAdapter
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_streams.*

class FollowedStreamsFragment : BaseStreamsFragment() {

    private lateinit var viewModel: FollowedStreamsViewModel

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FollowedStreamsViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = StreamsAdapter()
        recyclerViewLayout.recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        val mainViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.user.observe(viewLifecycleOwner, Observer {
            viewModel.setUser(it!!)
        })
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

}
