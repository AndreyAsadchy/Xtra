package com.github.exact7.xtra.ui.videos.game

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.video.Period
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import kotlinx.android.synthetic.main.fragment_videos.*

class GameVideosFragment : BaseVideosFragment(), GameVideosSortDialog.OnFilter {

    private lateinit var viewModel: GameVideosViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameVideosViewModel::class.java)
        binding.viewModel = viewModel
        binding.sortText = viewModel.sortText
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
        sortBar.setOnClickListener { GameVideosSortDialog.newInstance(viewModel.sort, viewModel.period).show(childFragmentManager, null) }
    }

    override fun initialize() {
        viewModel.setGame(arguments?.getParcelable("game") as Game)
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        adapter.submitList(null)
        viewModel.filter(sort, period, getString(R.string.sort_and_period, sortText, periodText))
    }
}
