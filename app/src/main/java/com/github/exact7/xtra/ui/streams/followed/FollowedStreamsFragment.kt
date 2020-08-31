package com.github.exact7.xtra.ui.streams.followed

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.ui.streams.StreamsCompactAdapter
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.prefs

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel by viewModels<FollowedStreamsViewModel> { viewModelFactory }

    override val adapter: BasePagedListAdapter<Stream> by lazy {
        if (!compactStreams) {
            super.adapter
        } else {
            val activity = requireActivity() as MainActivity
            StreamsCompactAdapter(this, activity, activity)
        }
    }

    private var compactStreams = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compactStreams = requireContext().prefs().getBoolean(C.COMPACT_STREAMS, false)
    }

    override fun initialize() {
        super.initialize()
        viewModel.init(User.get(requireContext()), !compactStreams)
    }
}
