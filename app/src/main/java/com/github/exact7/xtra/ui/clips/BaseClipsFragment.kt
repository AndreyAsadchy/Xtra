package com.github.exact7.xtra.ui.clips

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.databinding.FragmentClipsBinding
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.ui.Scrollable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_clips.*

abstract class BaseClipsFragment : BaseNetworkFragment(), Scrollable, RadioButtonDialogFragment.OnSortOptionChanged {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    protected lateinit var adapter: ClipsAdapter
    protected lateinit var binding: FragmentClipsBinding
    private var listener: OnClipSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClipSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnClipSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (isFragmentVisible) FragmentClipsBinding.inflate(inflater, container, false).let {
            binding = it
            it.setLifecycleOwner(this@BaseClipsFragment)
            it.root
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