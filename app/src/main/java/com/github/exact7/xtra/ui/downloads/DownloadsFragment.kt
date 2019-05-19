package com.github.exact7.xtra.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.exact7.xtra.databinding.FragmentDownloadsBinding
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
    private lateinit var binding: FragmentDownloadsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false).apply { lifecycleOwner = viewLifecycleOwner }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = createViewModel(DownloadsViewModel::class.java)
        binding.viewModel = viewModel
        val activity = requireActivity() as MainActivity
        val adapter = DownloadsAdapter(activity, viewModel::delete)
        viewModel.list.observe(this, Observer(adapter::submitList))
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    recyclerView?.smoothScrollToPosition(0)
                }
            }
        })
        search.setOnClickListener { activity.openSearch() }
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}
