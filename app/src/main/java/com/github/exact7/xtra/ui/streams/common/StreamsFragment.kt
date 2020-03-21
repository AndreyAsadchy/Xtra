package com.github.exact7.xtra.ui.streams.common

import androidx.fragment.app.viewModels
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.util.C

class StreamsFragment : BaseStreamsFragment<StreamsViewModel>() {

    override val viewModel by viewModels<StreamsViewModel> { viewModelFactory }

    override fun initialize() {
        super.initialize()
        viewModel.loadStreams(arguments?.getParcelable(C.GAME))
    }
}