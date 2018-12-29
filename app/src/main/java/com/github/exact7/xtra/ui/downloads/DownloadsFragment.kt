package com.github.exact7.xtra.ui.downloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.databinding.FragmentDownloadsBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.Scrollable
import kotlinx.android.synthetic.main.fragment_downloads.*
import javax.inject.Inject

class DownloadsFragment : androidx.fragment.app.Fragment(), Injectable, Scrollable {

    interface OnVideoSelectedListener {
        fun startOfflineVideo(video: OfflineVideo)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentDownloadsBinding
    private var listener: OnVideoSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnVideoSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnVideoSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false).apply { setLifecycleOwner(viewLifecycleOwner) }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(DownloadsViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = DownloadsAdapter(listener!!, viewModel::delete)
        viewModel.list.observe(this, Observer(adapter::submitList))
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        })
        recyclerView.adapter = adapter
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}
