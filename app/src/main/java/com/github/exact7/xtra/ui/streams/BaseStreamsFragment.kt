package com.github.exact7.xtra.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class BaseStreamsFragment<VM : PagedListViewModel<Stream>> : PagedListFragment<Stream, VM>(), Scrollable {

    interface OnStreamSelectedListener {
        fun startStream(stream: Stream)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_streams, container, false)
    }

    override fun createAdapter(): PagedListAdapter<Stream, *> {
        return StreamsAdapter(requireActivity() as MainActivity)
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}
