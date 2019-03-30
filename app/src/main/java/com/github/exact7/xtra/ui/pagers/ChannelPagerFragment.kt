package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentChannelBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C
import javax.inject.Inject


class ChannelPagerFragment : MediaPagerFragment(), Injectable, FollowFragment {

    private lateinit var binding: FragmentChannelBinding
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentChannelBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChannelPagerViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.loadStream(requireArguments().getParcelable(C.CHANNEL)!!)
        val activity = requireActivity() as MainActivity
//        viewModel.stream.observe(viewLifecycleOwner, Observer {
//            it.stream?.let { s -> watchLive.setOnClickListener { activity.startStream(s) } }
//        })
        setAdapter(ChannelPagerAdapter(activity, childFragmentManager, requireArguments()))
//        ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java).user.observe(viewLifecycleOwner, Observer {
//            if (it is LoggedIn) {
//                initializeFollow(this, viewModel, view.findViewById(R.id.follow), it)
//            }
//        })
    }

    companion object {
        fun newInstance(channel: Channel) = ChannelPagerFragment().apply { arguments = bundleOf(C.CHANNEL to channel) }
    }
}