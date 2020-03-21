package com.github.exact7.xtra.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.download.VideoDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class BaseVideosFragment<VM : BaseVideosViewModel> : PagedListFragment<Video, VM, BaseVideosAdapter>(), Scrollable, HasDownloadDialog {

    interface OnVideoSelectedListener {
        fun startVideo(video: Video, offset: Double? = null)
    }

    override val adapter: BaseVideosAdapter by lazy {
        val activity = requireActivity() as MainActivity
        VideosAdapter(activity, activity) {
            lastSelectedItem = it
            showDownloadDialog()
        }
    }

    var lastSelectedItem: Video? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastSelectedItem = savedInstanceState?.getParcelable(C.KEY_LAST_SELECTED_ITEM)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun initialize() {
        super.initialize()
        viewModel.positions.observe(viewLifecycleOwner, Observer {
            adapter.setVideoPositions(it)
        })
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        lastSelectedItem?.let {
            VideoDownloadDialog.newInstance(it).show(childFragmentManager, null)
        }
    }
}