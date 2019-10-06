package com.github.exact7.xtra.ui.clips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.ClipDownloadDialog
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class BaseClipsFragment<VM : PagedListViewModel<Clip>> : PagedListFragment<Clip, VM, BasePagedListAdapter<Clip>>(), Scrollable, RadioButtonDialogFragment.OnSortOptionChanged, HasDownloadDialog {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    var lastSelectedItem: Clip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastSelectedItem = savedInstanceState?.getParcelable(C.KEY_LAST_SELECTED_ITEM)
    }

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
