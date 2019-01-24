package com.github.exact7.xtra.ui.clips

import android.content.Context
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
    private var listener: OnClipSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClipSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnClipSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentClipsBinding.inflate(inflater, container, false).let {
            binding = it
            it.setLifecycleOwner(viewLifecycleOwner)
            it.root.recyclerView.adapter = ClipsAdapter(listener!!).also { a -> adapter = a }
            it.root
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        ClipDownloadDialog.newInstance(adapter.lastSelectedItem).show(childFragmentManager, null)
    }
}
