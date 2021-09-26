package com.github.andreyasadchy.xtra.ui.clips.followed

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.clips.ClipsAdapter
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*
import kotlinx.android.synthetic.main.sort_bar.*

class FollowedClipsFragment : BaseClipsFragment<FollowedClipsViewModel>() {

    override val viewModel by viewModels<FollowedClipsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Clip> by lazy {
        val activity = requireActivity() as MainActivity
        ClipsAdapter(this, activity, activity) {
            lastSelectedItem = it
            showDownloadDialog()
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.setUser(User.get(requireContext()))
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        adapter.submitList(null)
        viewModel.setTrending(tag == R.string.trending, index, text)
    }
}