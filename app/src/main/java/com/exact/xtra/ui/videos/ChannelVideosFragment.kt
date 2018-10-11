package com.exact.xtra.ui.videos

import android.content.Context
import android.os.Bundle
import android.view.View

import com.exact.xtra.R
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.util.C
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class ChannelVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnOptionSelectedListener {

    private companion object {
        val sortOptions = listOf(R.string.upload_date, R.string.view_count)
        const val DEFAULT_INDEX = 0
        const val TAG = "ChannelVideos"
    }

    private lateinit var channelId: Any

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelId = arguments?.get("channelId")!!
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, DEFAULT_INDEX, TAG) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            if (!viewModel.isInitialized()) {
                viewModel.sortText.postValue(getString(sortOptions[requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE).getInt(TAG, DEFAULT_INDEX)]))
                viewModel.sort = Sort.TIME
            }
            loadData()
            initDefaultSortTextObserver()
        }
    }

    override fun loadData(override: Boolean) {
        viewModel.loadChannelVideos(channelId = channelId, reload = override)
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.sortText != text) {
            viewModel.sort = if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS
            viewModel.sortText.postValue(text)
            loadData(true)
        }
    }
}
