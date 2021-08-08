package com.github.andreyasadchy.xtra.ui.search.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.repository.LoadingState
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.search.Searchable
import com.github.andreyasadchy.xtra.util.gone
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_search.*

class GameSearchFragment : BaseNetworkFragment(), Searchable {

    private val viewModel by viewModels<GameSearchViewModel> { viewModelFactory }
    private lateinit var adapter: GameSearchAdapter

    private var isInitialized = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.common_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        recyclerView.adapter = GameSearchAdapter(this, activity).also { adapter = it }
        swipeRefresh.isEnabled = false
    }

    override fun initialize() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            nothing_here.isVisible = it.isEmpty()
        })
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            val isLoading = it == LoadingState.LOADING
            progressBar.isVisible = isLoading
            if (isLoading) {
                nothing_here.gone()
            }
        })
        isInitialized = true
        search(requireParentFragment().search.query.toString())
    }

    override fun search(query: String) {
        if (isInitialized) {
            if (query.isNotEmpty()) {
                viewModel.setQuery(query)
            } else {
                adapter.submitList(null)
                nothing_here?.gone()
            }
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}