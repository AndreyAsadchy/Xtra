package com.github.exact7.xtra.ui.pagers

import androidx.fragment.app.Fragment
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.top.TopVideosFragment

class TopFragment : MediaFragment() {

    companion object {
        fun newInstance() = TopFragment()
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> StreamsFragment()
            1 -> TopVideosFragment()
            else -> ClipsFragment()
        }
    }
}