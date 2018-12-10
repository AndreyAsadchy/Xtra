package com.github.exact7.xtra.ui.streams.common

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment

class StreamsFragment : BaseStreamsFragment() {

    private lateinit var viewModel: StreamsViewModel

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamsViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        viewModel.loadStreams(arguments?.getParcelable("game"))
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}
