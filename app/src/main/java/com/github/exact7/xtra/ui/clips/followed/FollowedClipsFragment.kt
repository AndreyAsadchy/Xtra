package com.github.exact7.xtra.ui.clips.followed

import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.clips.BaseClipsFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*

class FollowedClipsFragment : BaseClipsFragment() {

    override lateinit var viewModel: FollowedClipsViewModel

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
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.setTrending(tag == R.string.trending, index, text)
    }
}
