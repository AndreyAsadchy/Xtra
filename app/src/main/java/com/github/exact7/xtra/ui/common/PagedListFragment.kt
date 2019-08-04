package com.github.exact7.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class PagedListFragment<T, VM : PagedListViewModel<T>> : BaseNetworkFragment() {

    protected lateinit var viewModel: VM
    protected lateinit var adapter: PagedListAdapter<T, *>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        recyclerView.adapter = createAdapter().also {
            adapter = it
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart < (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()) {
                        recyclerView.scrollToPosition(0)
                    }
                }
            })
        }
    }

    override fun initialize() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            nothing_here.visible(it.isEmpty())
        })
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            val isLoading = it == LoadingState.LOADING
            val isListEmpty = adapter.currentList.isNullOrEmpty()
            if (isLoading) {
                nothing_here.gone()
            }
            progressBar.visible(isLoading && isListEmpty)
            if (swipeRefresh.isEnabled) {
                swipeRefresh.isRefreshing = isLoading && !isListEmpty
            }
        })
        viewModel.pagingState.observe(viewLifecycleOwner, Observer {
            if (adapter is BasePagedListAdapter) {
                (adapter as BasePagedListAdapter).setPagingState(it)
            }
        })
        if (swipeRefresh.isEnabled) {
            swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    protected abstract fun createAdapter(): PagedListAdapter<T, *>
    protected abstract fun createViewModel(): VM
}