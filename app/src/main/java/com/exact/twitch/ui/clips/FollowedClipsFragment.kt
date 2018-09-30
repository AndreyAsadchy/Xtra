package com.exact.twitch.ui.clips

import android.content.Context
import android.os.Bundle
import android.view.View
import com.exact.twitch.R
import com.exact.twitch.util.C
import com.exact.twitch.util.FragmentUtils
import com.exact.twitch.util.TwitchApiHelper
import kotlinx.android.synthetic.main.fragment_clips.*

class FollowedClipsFragment : BaseClipsFragment() {

    private companion object {
        val sortOptions = listOf(R.string.trending, R.string.view_count)
        const val DEFAULT_INDEX = 1
        const val TAG = "FollowedClips"
    }

    private var userToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userToken = TwitchApiHelper.getUserToken(requireActivity())
        sortByRl.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, DEFAULT_INDEX, TAG) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible && !viewModel.isInitialized()) {
            viewModel.sortText.postValue(getString(sortOptions[DEFAULT_INDEX]))
        }
    }

    override fun loadData(override: Boolean) {
        if (userToken != null) { //TODO add if not authorized
            viewModel.loadFollowedClips(userToken as String, override)
        }
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.sortText.value != text) {
            viewModel.trending = tag == R.string.trending
            viewModel.sortText.postValue(text)
            loadData(true)
        }
    }
}
