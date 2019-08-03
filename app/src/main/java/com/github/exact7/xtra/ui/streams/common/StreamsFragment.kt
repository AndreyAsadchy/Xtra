package com.github.exact7.xtra.ui.streams.common

import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.util.C

class StreamsFragment : BaseStreamsFragment<StreamsViewModel>() {

    override fun initialize() {
        super.initialize()
        viewModel.loadStreams(arguments?.getParcelable(C.GAME))
    }

    override fun createViewModel(): StreamsViewModel = getViewModel()
}
