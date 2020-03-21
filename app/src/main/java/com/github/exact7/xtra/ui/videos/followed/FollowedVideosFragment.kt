package com.github.exact7.xtra.ui.videos.followed

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class FollowedVideosFragment : BaseVideosFragment<FollowedVideosViewModel>(), RadioButtonDialogFragment.OnSortOptionChanged {

    override val viewModel by viewModels<FollowedVideosViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        viewModel.setUser(User.get(requireContext()))
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.sort(if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS, index, text)
    }
}
