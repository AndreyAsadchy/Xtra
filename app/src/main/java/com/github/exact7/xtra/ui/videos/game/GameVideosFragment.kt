package com.github.exact7.xtra.ui.videos.game

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Period
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.*

class GameVideosFragment : BaseVideosFragment<GameVideosViewModel>(), GameVideosSortDialog.OnFilter {

    override val viewModel by viewModels<GameVideosViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        viewModel.setGame(requireArguments().getParcelable(C.GAME)!!)
        sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        adapter.submitList(null)
        viewModel.filter(sort, period, getString(R.string.sort_and_period, sortText, periodText))
    }
}
