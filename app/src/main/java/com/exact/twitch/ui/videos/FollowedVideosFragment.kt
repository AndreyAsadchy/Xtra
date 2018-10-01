package com.exact.twitch.ui.videos

import android.os.Bundle
import android.view.View
import com.exact.twitch.R
import com.exact.twitch.model.User
import com.exact.twitch.ui.fragment.RadioButtonDialogFragment
import com.exact.twitch.util.C
import com.exact.twitch.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class FollowedVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnOptionSelectedListener {

    private companion object {
        val sortOptions = listOf(R.string.upload_date, R.string.view_count)
        const val DEFAULT_INDEX = 0
        const val TAG = "FollowedVideos"
    }

    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments!!.getParcelable(C.USER)!!
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
        viewModel.loadFollowedVideos(userToken = user.token, reload = override)
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.sortText.value != text) {
            viewModel.sort = if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS
            viewModel.sortText.postValue(text)
            loadData(true)
        }
    }
}
