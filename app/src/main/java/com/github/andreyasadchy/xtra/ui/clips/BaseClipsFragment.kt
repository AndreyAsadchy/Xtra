package com.github.andreyasadchy.xtra.ui.clips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.download.ClipDownloadDialog
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class BaseClipsFragment<VM : PagedListViewModel<Clip>> : PagedListFragment<Clip, VM, BasePagedListAdapter<Clip>>(), Scrollable, RadioButtonDialogFragment.OnSortOptionChanged, HasDownloadDialog {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    var lastSelectedItem: Clip? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_clips, container, false)
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        lastSelectedItem?.let {
            ClipDownloadDialog.newInstance(it).show(childFragmentManager, null)
        }
    }
}
