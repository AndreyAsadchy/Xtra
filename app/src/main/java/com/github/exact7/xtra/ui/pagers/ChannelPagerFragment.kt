package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.github.exact7.xtra.databinding.FragmentChannelBinding
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.util.C

class ChannelPagerFragment : MediaPagerFragment() {

    private lateinit var binding: FragmentChannelBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentChannelBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(viewLifecycleOwner)
                binding.root
            }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.channel = arguments!!.getParcelable(C.CHANNEL)
        setAdapter(ChannelPagerAdapter(requireActivity(), childFragmentManager, arguments!!))
    }

    companion object {
        fun newInstance(channel: Channel) = ChannelPagerFragment().apply { arguments = bundleOf(C.CHANNEL to channel) }
    }
}