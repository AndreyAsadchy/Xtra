package com.github.exact7.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class PagedListFragment<T, VM : PagedListViewModel<T>> : BaseNetworkFragment() {

    protected lateinit var viewModel: VM
    protected lateinit var adapter: PagedListAdapter<T, *>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        recyclerView.adapter = createAdapter().also { adapter = it }
    }

    override fun initialize() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            val isLoading = it == LoadingState.LOADING
            val isListEmpty = adapter.currentList.isNullOrEmpty()
            progressBar.visible(isLoading && isListEmpty)
            swipeRefresh.isRefreshing = isLoading && !isListEmpty
        })
        swipeRefresh.setOnRefreshListener { viewModel.refresh() }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    protected abstract fun createAdapter(): PagedListAdapter<T, *>
    protected abstract fun createViewModel(): VM
}