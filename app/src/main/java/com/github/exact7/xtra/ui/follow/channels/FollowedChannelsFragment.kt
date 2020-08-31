package com.github.exact7.xtra.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.follows.Follow
import com.github.exact7.xtra.model.kraken.follows.Order
import com.github.exact7.xtra.model.kraken.follows.Sort
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_followed_channels.*
import kotlinx.android.synthetic.main.sort_bar.*

class FollowedChannelsFragment : PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(), FollowedChannelsSortDialog.OnFilter, Scrollable {

    override val viewModel by viewModels<FollowedChannelsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Follow> by lazy {
        val activity = requireActivity() as MainActivity
        FollowedChannelsAdapter(this, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_followed_channels, container, false)
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        viewModel.setUser(User.get(requireContext()))
        sortBar.setOnClickListener { FollowedChannelsSortDialog.newInstance(viewModel.sort, viewModel.order).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, order: Order, orderText: CharSequence) {
        adapter.submitList(null)
        viewModel.filter(sort, order, getString(R.string.sort_and_order, sortText, orderText))
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}