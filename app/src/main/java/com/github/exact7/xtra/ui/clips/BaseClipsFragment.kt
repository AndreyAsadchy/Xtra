package com.github.exact7.xtra.ui.clips

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentClipsBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.ui.Scrollable
import com.github.exact7.xtra.ui.fragment.LazyFragment
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_clips.*
import javax.inject.Inject

abstract class BaseClipsFragment : LazyFragment(), Injectable, Scrollable, Loadable, RadioButtonDialogFragment.OnSortOptionChanged {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var viewModel: ClipsViewModel
    protected lateinit var adapter: ClipsAdapter
    private lateinit var binding: FragmentClipsBinding
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(ClipsViewModel::class.java)
            binding.viewModel = viewModel
            adapter = ClipsAdapter(listener!!)
            recyclerViewLayout.recyclerView.adapter = adapter
            if (!viewModel.isInitialized()) {
                initializeViewModel()
            }
            loadData()
            viewModel.list.observe(this, Observer {
                adapter.submitList(it)
            })
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }

    abstract fun initializeViewModel()
}
