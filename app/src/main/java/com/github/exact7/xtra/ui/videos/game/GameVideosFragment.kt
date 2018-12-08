package com.github.exact7.xtra.ui.videos.game

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.ui.videos.Period
import com.github.exact7.xtra.ui.videos.Sort
import kotlinx.android.synthetic.main.fragment_videos.*

class GameVideosFragment : BaseVideosFragment(), GameVideosSortDialog.OnFilter {

    private lateinit var viewModel: GameVideosViewModel

    override fun initialize() {
        if (isFragmentVisible) {
            super.initialize()
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameVideosViewModel::class.java)
            binding.viewModel = viewModel
            binding.sortText = viewModel.sortText
            viewModel.list.observe(this, Observer {
                adapter.submitList(it)
            })
            viewModel.setGame(arguments?.getParcelable("game") as Game)
            sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period).show(childFragmentManager, null) }
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        adapter.submitList(null)
        viewModel.filter(sort, period, getString(R.string.sort_and_period, sortText, periodText))
    }
}
