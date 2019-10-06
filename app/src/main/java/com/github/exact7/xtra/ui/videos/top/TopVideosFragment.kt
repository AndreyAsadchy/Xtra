package com.github.exact7.xtra.ui.videos.top

import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Period
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.ui.videos.VideosAdapter
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class TopVideosFragment : BaseVideosFragment<TopVideosViewModel>(), RadioButtonDialogFragment.OnSortOptionChanged {

    override fun createViewModel(): TopVideosViewModel = getViewModel()

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
        viewModel.positions.observe(viewLifecycleOwner, Observer {
            println("SET $it")
            (adapter as VideosAdapter).setPositions(it)
        })
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        val period = when(tag) {
            R.string.today -> Period.DAY
            R.string.this_week -> Period.WEEK
            R.string.this_month -> Period.MONTH
            R.string.all_time -> Period.ALL
            else -> throw IllegalArgumentException()
        }
        adapter.submitList(null)
        viewModel.filter(period, index, text)
    }
}
