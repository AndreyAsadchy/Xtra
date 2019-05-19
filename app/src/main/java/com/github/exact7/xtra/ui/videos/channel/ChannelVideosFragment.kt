package com.github.exact7.xtra.ui.videos.channel

import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class ChannelVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    override lateinit var viewModel: ChannelVideosViewModel

    override fun initialize() {
        viewModel = createViewModel(ChannelVideosViewModel::class.java)
        binding.viewModel = viewModel
        binding.sortText = viewModel.sortText
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
        viewModel.setChannelId(requireArguments().getParcelable<Channel>(C.CHANNEL)!!.id)
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.setSort(if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS, index, text)
    }
}
