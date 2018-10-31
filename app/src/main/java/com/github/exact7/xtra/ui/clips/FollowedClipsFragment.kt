package com.github.exact7.xtra.ui.clips

import android.os.Bundle
import android.view.View
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*

class FollowedClipsFragment : BaseClipsFragment() {

    private companion object {
        val sortOptions = listOf(R.string.trending, R.string.view_count)
        const val DEFAULT_INDEX = 1
    }

    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments!!.getParcelable(C.USER)!!
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, viewModel.selectedIndex) }
    }

    override fun initializeViewModel() {
        viewModel.selectedIndex = DEFAULT_INDEX
        viewModel.sortText.value = getString(sortOptions[DEFAULT_INDEX])
    }

    override fun loadData(override: Boolean) {
        viewModel.loadFollowedClips(userToken = user.token, reload = override)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.trending = tag == R.string.trending
        viewModel.sortText.postValue(text)
        viewModel.selectedIndex = index
        viewModel.loadedInitial.value = null
        adapter.submitList(null)
        loadData(true)
    }
}
