package com.github.exact7.xtra.ui.downloads

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_downloads.*
import javax.inject.Inject

class DownloadsFragment : Fragment(), Injectable, Scrollable {

    interface OnVideoSelectedListener {
        fun startOfflineVideo(video: OfflineVideo)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(DownloadsViewModel::class.java)
        val activity = requireActivity() as MainActivity
        val adapter = DownloadsAdapter(activity) {
            val delete = getString(R.string.delete)
            AlertDialog.Builder(activity)
                    .setTitle(delete)
                    .setMessage(getString(R.string.are_you_sure))
                    .setPositiveButton(delete) { _, _ -> viewModel.delete(it) }
                    .setNegativeButton(getString(android.R.string.cancel), null)
        }
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
            text.isVisible = it.isEmpty()
        })
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    recyclerView?.smoothScrollToPosition(0)
                }
            }
        })
        search.setOnClickListener { activity.openSearch() }
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}
