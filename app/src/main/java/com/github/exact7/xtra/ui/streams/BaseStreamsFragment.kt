package com.github.exact7.xtra.ui.streams

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentStreamsBinding
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
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
    private var listener: OnStreamSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStreamSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnStreamSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentStreamsBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(viewLifecycleOwner)
                it.root.recyclerView.adapter =  StreamsAdapter(listener!!).also { a -> adapter = a }
                it.root
            }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
