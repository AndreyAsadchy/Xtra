package com.github.exact7.xtra.ui.search.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.search.Searchable
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.simple_recycler_view_layout.*

class GameSearchFragment : BaseNetworkFragment(), Searchable {

    private lateinit var viewModel: GameSearchViewModel
    private lateinit var adapter: GameSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = GameSearchAdapter(activity).also { adapter = it }
    }

    override fun initialize() {
        viewModel = getViewModel()
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            progressBar.visible(it == LoadingState.LOADING)
        })
    }

    override fun search(query: String) {
        if (query.isNotEmpty()) {
            viewModel.setQuery(query)
        } else {
            adapter.submitList(null)
        }
    }

    override fun onNetworkRestored() {

    }
}