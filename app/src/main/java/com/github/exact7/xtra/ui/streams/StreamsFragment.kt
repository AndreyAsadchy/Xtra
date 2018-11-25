package com.github.exact7.xtra.ui.streams

import android.os.Bundle
import android.view.View
import com.github.exact7.xtra.model.game.Game

class StreamsFragment : BaseStreamsFragment() {

    private var game: Game? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game = arguments?.getParcelable("game")
    }

    override fun initialize() {
        super.initialize()
        viewModel.
    }

//    override fun loadData(override: Boolean) {
//        viewModel.loadStreams(game?.info?.name)
//    }
}
