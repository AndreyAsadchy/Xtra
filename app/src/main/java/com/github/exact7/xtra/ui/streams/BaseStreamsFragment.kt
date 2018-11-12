package com.github.exact7.xtra.ui.streams

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentStreamsBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.stream.Stream
import com.github.exact7.xtra.ui.Scrollable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_streams.*
import javax.inject.Inject

abstract class BaseStreamsFragment : BaseNetworkFragment(), Injectable, Scrollable {

    interface OnStreamSelectedListener {
        fun startStream(stream: Stream)
    }

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var viewModel: StreamsViewModel
    private lateinit var binding: FragmentStreamsBinding
    private var listener: OnStreamSelectedListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnStreamSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnStreamSelectedListener")
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentStreamsBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(this)
                it.root
            }

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamsViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = StreamsAdapter()
        recyclerViewLayout.recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
