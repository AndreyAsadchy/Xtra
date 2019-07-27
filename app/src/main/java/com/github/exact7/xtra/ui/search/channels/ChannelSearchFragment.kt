package com.github.exact7.xtra.ui.search.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.search.SearchViewModel
import kotlinx.android.synthetic.main.simple_recycler_view_layout.*

class ChannelSearchFragment : BaseNetworkFragment() {

    private lateinit var adapter: ChannelsSearchAdapter
    override lateinit var viewModel: ChannelSearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = ChannelsSearchAdapter(activity).also { adapter = it }
    }

    override fun initialize() {
        viewModel = createViewModel()
        ViewModelProviders.of(requireParentFragment()).get(SearchViewModel::class.java).query.observe(viewLifecycleOwner, Observer {
            viewModel.setQuery(it)
        })
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun onNetworkRestored() {

    }
}