package com.github.exact7.xtra.ui.videos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentVideosBinding
import com.github.exact7.xtra.model.video.Video
import com.github.exact7.xtra.ui.Scrollable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_videos.*

abstract class BaseVideosFragment : BaseNetworkFragment(), Scrollable {

    interface OnVideoSelectedListener {
        fun startVideo(video: Video)
    }

    protected lateinit var binding: FragmentVideosBinding
    private var listener: OnVideoSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnVideoSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnVideoSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (isFragmentVisible) {
            FragmentVideosBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(this@BaseVideosFragment)
                it.root
            }
        } else {
            null
        }
    }

    override fun initialize() {
        binding.listener = listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
