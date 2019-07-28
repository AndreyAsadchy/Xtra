package com.github.exact7.xtra.ui.clips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentClipsBinding
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.clips.common.ChannelClipsAdapter
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.ClipDownloadDialog
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.videos.TempBaseAdapter
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*

abstract class BaseClipsFragment : BaseNetworkFragment(), Scrollable, RadioButtonDialogFragment.OnSortOptionChanged, HasDownloadDialog {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    protected abstract val viewModel: PagedListViewModel<Clip>
    protected lateinit var adapter: TempBaseAdapter<Clip, *>
        private set
    protected lateinit var binding: FragmentClipsBinding
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentClipsBinding.inflate(inflater, container, false).let {
            binding = it
            it.lifecycleOwner = viewLifecycleOwner
            val activity = requireActivity() as MainActivity
            adapter = if (this is ClipsFragment && isChannel) {
                ChannelClipsAdapter(activity)
            } else {
                ClipsAdapter(activity)
            }
            it.root.recyclerView.adapter = adapter
//            it.root.recyclerView.adapter = ClipsAdapter(requireActivity() as MainActivity).also { a -> adapter = a }
            it.root
        }
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        ClipDownloadDialog.newInstance(adapter.lastSelectedItem!!).show(childFragmentManager, null)
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}
