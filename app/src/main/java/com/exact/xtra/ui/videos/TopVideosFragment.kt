package com.exact.xtra.ui.videos

import android.content.Context
import android.os.Bundle
import android.view.View

import com.exact.xtra.R
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.util.C
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class TopVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private companion object {
        val sortOptions = listOf(R.string.today, R.string.this_week, R.string.this_month, R.string.all_time)
        const val DEFAULT_INDEX = 1
        const val TAG = "TopVideos"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, DEFAULT_INDEX, TAG) }
    }

    override fun initializeViewModel() {
        viewModel.sort = Sort.VIEWS
        val index = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE).getInt(TAG, DEFAULT_INDEX)
        viewModel.sortText.postValue(getString(sortOptions[index]))
        viewModel.period = Period.values()[index]
    }

    override fun loadData(override: Boolean) {
        viewModel.loadVideos(reload = override)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.period = when(tag) {
            R.string.today -> Period.DAY
            R.string.this_week -> Period.WEEK
            R.string.this_month -> Period.MONTH
            R.string.all_time -> Period.ALL
            else -> null
        }
        viewModel.sortText.postValue(text)
        loadData(true)
    }
}
