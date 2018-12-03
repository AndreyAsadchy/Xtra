package com.github.exact7.xtra.ui.videos.game

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.ui.videos.Period
import com.github.exact7.xtra.ui.videos.Sort
import kotlinx.android.synthetic.main.fragment_videos.*

class GameVideosFragment : BaseVideosFragment(), GameVideosSortDialog.OnFilterApplied {

    private lateinit var viewModel: GameVideosViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period).show(childFragmentManager, null) }
    }

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
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun onApply(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        viewModel.filter(sort, period, getString(R.string.sort_and_period, sort, period))
    }
}
