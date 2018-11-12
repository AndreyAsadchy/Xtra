package com.github.exact7.xtra.ui.videos

import android.os.Bundle
import android.view.View
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*

class TopVideosFragment : BaseVideosFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private companion object {
        val sortOptions = listOf(R.string.today, R.string.this_week, R.string.this_month, R.string.all_time)
        const val DEFAULT_INDEX = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, viewModel.selectedIndex) }
    }

    override fun initializeViewModel() {
        viewModel.sort = Sort.VIEWS
        viewModel.period = Period.WEEK
        viewModel.sortText.value = getString(sortOptions[DEFAULT_INDEX])
        viewModel.selectedIndex = DEFAULT_INDEX
    }

//    override fun loadData(override: Boolean) {
//        viewModel.loadVideos(reload = override)
//    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.period = when(tag) {
            R.string.today -> Period.DAY
            R.string.this_week -> Period.WEEK
            R.string.this_month -> Period.MONTH
            R.string.all_time -> Period.ALL
            else -> null
        }
        viewModel.sortText.postValue(text)
        viewModel.selectedIndex = index
//        viewModel.loadedInitial.value = null
        adapter.submitList(null)
//        loadData(true)
    }
}
