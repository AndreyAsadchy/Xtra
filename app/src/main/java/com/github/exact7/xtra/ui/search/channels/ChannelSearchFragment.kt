package com.github.exact7.xtra.ui.search.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.search.Searchable
import com.github.exact7.xtra.util.gone
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

class ChannelSearchFragment : PagedListFragment<Channel, ChannelSearchViewModel, BasePagedListAdapter<Channel>>(), Searchable {

    override val viewModel by viewModels<ChannelSearchViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Channel> by lazy { ChannelSearchAdapter(this, requireActivity() as MainActivity) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.common_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.isEnabled = false
    }

    override fun search(query: String) {
        if (query.isNotEmpty()) { //TODO same query doesn't fire
            viewModel.setQuery(query)
        } else {
            adapter.submitList(null)
            nothing_here.gone()
        }
    }
}