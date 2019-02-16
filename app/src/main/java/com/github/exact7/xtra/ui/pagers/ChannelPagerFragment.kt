package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentChannelBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.fragment_channel.*
import javax.inject.Inject

class ChannelPagerFragment : MediaPagerFragment(), Injectable {

    private lateinit var binding: FragmentChannelBinding
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentChannelBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(viewLifecycleOwner)
                binding.root
            }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChannelPagerViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.loadStream(arguments!!.getParcelable<Channel>(C.CHANNEL)!!.id)
        val activity = requireActivity() as MainActivity
        viewModel.stream.observe(viewLifecycleOwner, Observer {
            it.stream?.let { s -> watchLive.setOnClickListener { activity.startStream(s) } }
        })
        setAdapter(ChannelPagerAdapter(activity, childFragmentManager, arguments!!))
    }

    companion object {
        fun newInstance(channel: Channel) = ChannelPagerFragment().apply { arguments = bundleOf(C.CHANNEL to channel) }
    }
}