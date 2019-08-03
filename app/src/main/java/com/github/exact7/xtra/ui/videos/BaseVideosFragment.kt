package com.github.exact7.xtra.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.common.PagedListFragment
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.videos.channel.ChannelVideosAdapter
import com.github.exact7.xtra.ui.videos.channel.ChannelVideosFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class BaseVideosFragment<VM : PagedListViewModel<Video>> : PagedListFragment<Video, VM>(), Scrollable, HasDownloadDialog {

    interface OnVideoSelectedListener {
        fun startVideo(video: Video)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun createAdapter(): PagedListAdapter<Video, *> {
        val activity = requireActivity() as MainActivity
        return if (this !is ChannelVideosFragment) {
            VideosAdapter(activity)
        } else {
            ChannelVideosAdapter(activity)
        }
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
//        VideoDownloadDialog.newInstance(video = adapter.lastSelectedItem!!).show(childFragmentManager, null)
    }
}