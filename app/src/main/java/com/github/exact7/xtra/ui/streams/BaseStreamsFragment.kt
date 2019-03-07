package com.github.exact7.xtra.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentStreamsBinding
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_streams.*

abstract class BaseStreamsFragment : BaseNetworkFragment(), Scrollable {

    interface OnStreamSelectedListener {
        fun startStream(stream: Stream)
    }

    protected lateinit var adapter: StreamsAdapter
        private set
    protected lateinit var binding: FragmentStreamsBinding
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentStreamsBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                it.root.recyclerView.adapter =  StreamsAdapter(requireActivity() as MainActivity).also { a -> adapter = a }
                it.root
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentUtils.setRecyclerViewSpanCount(recyclerViewLayout.recyclerView)
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
