package com.github.exact7.xtra.ui.videos

import android.os.Bundle
import android.view.View
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.game.Game
import kotlinx.android.synthetic.main.fragment_videos.*

class GameVideosFragment : BaseVideosFragment(), VideosSortDialog.OnFilterApplied {

    private lateinit var game: Game

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game = arguments?.getParcelable("game")!!
        sortBar.setOnClickListener { VideosSortDialog.newInstance(viewModel.sort, viewModel.period!!).show(childFragmentManager, null) }
    }

    override fun initializeViewModel() {
        viewModel.sort = Sort.VIEWS
        viewModel.period = Period.WEEK
        setSortText(getString(R.string.view_count), getString(R.string.this_week))
    }

//    override fun loadData(override: Boolean) {
//        viewModel.loadVideos(game = game.info.name, reload = override)
//    }

    override fun onApply(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        var shouldReload = false
        if (viewModel.sort != sort) {
            shouldReload = true
            viewModel.sort = sort
        }
        if (viewModel.period != period) {
            shouldReload = true
            viewModel.period = period
        }
        if (shouldReload) {
            setSortText(sortText, periodText)
//            viewModel.loadedInitial.value = null
            adapter.submitList(null)
//            loadData(true)
        }
    }

    private fun setSortText(sort: CharSequence, period: CharSequence) {
        viewModel.sortText.postValue(getString(R.string.sort_and_period, sort, period))
    }
}
