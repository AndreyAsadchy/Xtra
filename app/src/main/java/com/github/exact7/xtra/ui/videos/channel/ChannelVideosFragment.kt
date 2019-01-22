package com.github.exact7.xtra.ui.videos.channel

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class ChannelVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private lateinit var viewModel: ChannelVideosViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChannelVideosViewModel::class.java)
        binding.viewModel = viewModel
        binding.sortText = viewModel.sortText
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun initialize() {
        viewModel.setChannelId(arguments?.get("channelId")!!)
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.setSort(if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS, index, text)
    }
}
