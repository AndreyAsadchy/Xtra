package com.exact.twitch.ui.videos

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.View

import com.exact.twitch.R
import com.exact.twitch.ui.fragment.RadioButtonDialogFragment
import com.exact.twitch.util.C
import com.exact.twitch.util.FragmentUtils
import com.exact.twitch.util.TwitchApiHelper
import kotlinx.android.synthetic.main.fragment_videos.*

class FollowedVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnOptionSelectedListener {

    private companion object {
        val sortOptions = listOf(R.string.upload_date, R.string.view_count)
        const val DEFAULT_INDEX = 0
        const val TAG = "FollowedVideos"
    }

    private var userToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userToken = TwitchApiHelper.getUserToken(requireActivity())
        sortByRl.setOnClickListener{ FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, DEFAULT_INDEX, TAG) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            if (!viewModel.isInitialized()) {
                viewModel.sortText.postValue(getString(sortOptions[DEFAULT_INDEX]))
                viewModel.sort = Sort.TIME
            }
            initDefaultSortTextObserver()
        }
    }

    override fun loadData(override: Boolean) {
        if (userToken != null) {
            viewModel.loadFollowedVideos(userToken = userToken as String, reload = override)
        }
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.sortText.value != text) {
            viewModel.sort = if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS
            viewModel.sortText.postValue(text)
            loadData(true)
        }
    }
}
