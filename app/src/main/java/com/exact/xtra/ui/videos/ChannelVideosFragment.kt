package com.exact.xtra.ui.videos

import android.content.Context
import android.os.Bundle
import android.view.View

import com.exact.xtra.R
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.util.C
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class ChannelVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

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

    override fun initializeViewModel() {
        val index = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE).getInt(TAG, DEFAULT_INDEX)
        viewModel.sortText.postValue(getString(sortOptions[index]))
        viewModel.sort = if (index == 0) Sort.TIME else Sort.VIEWS
    }

    override fun loadData(override: Boolean) {
        viewModel.loadChannelVideos(channelId = channelId, reload = override)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) { //TODO move this to viewmodel and update there
        viewModel.sort = if (tag == R.string.upload_date) Sort.TIME else Sort.VIEWS
        viewModel.sortText.postValue(text)
        loadData(true)
    }
}
