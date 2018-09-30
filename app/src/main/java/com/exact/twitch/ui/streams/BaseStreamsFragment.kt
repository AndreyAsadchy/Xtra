package com.exact.twitch.ui.streams

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exact.twitch.R
import com.exact.twitch.databinding.FragmentStreamsBinding
import com.exact.twitch.di.Injectable
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.ui.Loadable
import com.exact.twitch.ui.Scrollable
import com.exact.twitch.ui.fragment.LazyFragment
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        if (isFragmentVisible) {
            binding = FragmentStreamsBinding.inflate(inflater, container, false).apply { setLifecycleOwner(this@BaseStreamsFragment) }
            binding.root
        } else {
            null
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewLayout.recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireActivity(), resources.getInteger(R.integer.media_columns))
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

    fun defaultOnActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
