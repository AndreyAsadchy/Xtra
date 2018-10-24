package com.exact.xtra.ui.videos

import android.os.Bundle
import android.view.View
import com.exact.xtra.R
import com.exact.xtra.model.User
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.util.C
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class FollowedVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private companion object {
        val sortOptions = listOf(R.string.upload_date, R.string.view_count)
        const val DEFAULT_INDEX = 0
    }

    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments!!.getParcelable(C.USER)!!
        sortBar.setOnClickListener{ FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, viewModel.selectedIndex) }
    }

    override fun initializeViewModel() {
        viewModel.sortText.value = getString(sortOptions[DEFAULT_INDEX])
        viewModel.sort = Sort.TIME
    }

    override fun loadData(override: Boolean) {
        viewModel.loadFollowedVideos(userToken = user.token, reload = override)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.sort = if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS
        viewModel.sortText.value = text
        viewModel.selectedIndex = index
        viewModel.loadedInitial.value = false
        adapter.submitList(null)
        loadData(true)
    }
}
