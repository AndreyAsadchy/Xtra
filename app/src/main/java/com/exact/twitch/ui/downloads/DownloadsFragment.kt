package com.exact.twitch.ui.downloads

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exact.twitch.databinding.FragmentDownloadsBinding
import com.exact.twitch.di.Injectable
import com.exact.twitch.model.OfflineVideo
import kotlinx.android.synthetic.main.fragment_downloads.*
import javax.inject.Inject

class DownloadsFragment : androidx.fragment.app.Fragment(), Injectable {

    interface OnVideoSelectedListener {
        fun startOfflineVideo(video: OfflineVideo)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentDownloadsBinding
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
        binding = FragmentDownloadsBinding.inflate(inflater, container, false).apply { setLifecycleOwner(this@DownloadsFragment) }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(DownloadsViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = DownloadsAdapter(listener!!)
        viewModel.load().observe(this, Observer(adapter::submitList))
        recyclerView.adapter = adapter
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
