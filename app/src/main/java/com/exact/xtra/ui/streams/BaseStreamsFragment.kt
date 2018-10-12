package com.exact.xtra.ui.streams

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.databinding.FragmentStreamsBinding
import com.exact.xtra.di.Injectable
import com.exact.xtra.model.stream.Stream
import com.exact.xtra.ui.Loadable
import com.exact.xtra.ui.Scrollable
import com.exact.xtra.ui.fragment.LazyFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_streams.*
import javax.inject.Inject

abstract class BaseStreamsFragment : LazyFragment(), Injectable, Scrollable, Loadable {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (isFragmentVisible) {
            FragmentStreamsBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(this@BaseStreamsFragment)
                it.root
            }
        } else {
            null
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamsViewModel::class.java)
            binding.viewModel = viewModel
            loadData()
            val adapter = StreamsAdapter(listener!!)
            recyclerViewLayout.recyclerView.adapter = adapter
            viewModel.list.observe(this, Observer {
                adapter.submitList(it)
            })
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
