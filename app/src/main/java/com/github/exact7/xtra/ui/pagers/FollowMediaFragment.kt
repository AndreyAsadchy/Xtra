package com.github.exact7.xtra.ui.pagers

import androidx.fragment.app.Fragment
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosFragment

class FollowMediaFragment : MediaFragment() {

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            else -> FollowedClipsFragment()
        }
    }
}