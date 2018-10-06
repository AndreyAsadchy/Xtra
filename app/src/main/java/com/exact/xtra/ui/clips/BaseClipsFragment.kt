package com.exact.xtra.ui.clips

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.R
import com.exact.xtra.databinding.FragmentClipsBinding
import com.exact.xtra.di.Injectable
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.ui.Loadable
import com.exact.xtra.ui.Scrollable
import com.exact.xtra.ui.fragment.LazyFragment
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_clips.*
import kotlinx.android.synthetic.main.sort_bar.view.*
import javax.inject.Inject

abstract class BaseClipsFragment : LazyFragment(), Injectable, Scrollable, Loadable, RadioButtonDialogFragment.OnOptionSelectedListener {

    interface OnClipSelectedListener {
        fun startClip(clip: Clip)
    }

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var viewModel: ClipsViewModel
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
        return if (isFragmentVisible) {
            binding = FragmentClipsBinding.inflate(inflater, container, false).apply { setLifecycleOwner(this@BaseClipsFragment) }
            binding.root
        } else {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewLayout.recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireActivity(), resources.getInteger(R.integer.media_columns))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(ClipsViewModel::class.java)
            binding.viewModel = viewModel
            val adapter = ClipsAdapter(listener!!)
            recyclerViewLayout.recyclerView.adapter = adapter
            loadData()
            viewModel.list.observe(this, Observer {
                adapter.submitList(it)
            })
            viewModel.sortText.observe(this, Observer(sortBar.sortText::setText))
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
