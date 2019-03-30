package com.github.exact7.xtra.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentVideosBinding
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.download.VideoDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*

abstract class BaseVideosFragment : BaseNetworkFragment(), Scrollable, HasDownloadDialog {

    interface OnVideoSelectedListener {
        fun startVideo(video: Video)
    }

    protected lateinit var adapter: VideosAdapter
        private set
    protected lateinit var binding: FragmentVideosBinding
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentVideosBinding.inflate(inflater, container, false).let {
            binding = it
            it.lifecycleOwner = viewLifecycleOwner
            it.root.recyclerView.adapter = VideosAdapter(requireActivity() as MainActivity).also { a -> adapter = a }
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentUtils.setRecyclerViewSpanCount(recyclerView)
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    override fun showDownloadDialog() {
        VideoDownloadDialog.newInstance(video = adapter.lastSelectedItem).show(childFragmentManager, null)
    }
}
