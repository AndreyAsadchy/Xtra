package com.github.exact7.xtra.ui.clips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentClipsBinding
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.ClipDownloadDialog
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_clips.*

abstract class BaseClipsFragment : BaseNetworkFragment(), Scrollable, RadioButtonDialogFragment.OnSortOptionChanged, HasDownloadDialog {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    protected lateinit var adapter: ClipsAdapter
        private set
    protected lateinit var binding: FragmentClipsBinding
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentClipsBinding.inflate(inflater, container, false).let {
            binding = it
            it.setLifecycleOwner(viewLifecycleOwner)
            it.root.recyclerView.adapter = ClipsAdapter(requireActivity() as MainActivity).also { a -> adapter = a }
            it.root
        }
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        ClipDownloadDialog.newInstance(adapter.lastSelectedItem).show(childFragmentManager, null)
    }
}
