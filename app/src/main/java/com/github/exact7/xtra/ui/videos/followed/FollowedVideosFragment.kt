package com.github.exact7.xtra.ui.videos.followed

import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class FollowedVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    override lateinit var viewModel: FollowedVideosViewModel

    override fun initialize() {
        viewModel = createViewModel()
        binding.viewModel = viewModel
        binding.sortText = viewModel.sortText
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
        getMainViewModel().user.observe(viewLifecycleOwner, Observer {
            viewModel.setUser(it)
        })
        sortBar.setOnClickListener{ FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.sort(if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS, index, text)
    }
}
