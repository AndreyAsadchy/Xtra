package com.github.exact7.xtra.ui.pagers

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosFragment
import com.github.exact7.xtra.util.C

class FollowFragment : MediaFragment() {

    companion object {
        fun newInstance(user: LoggedIn) = FollowFragment().apply { arguments = bundleOf(C.USER to user) }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            else -> FollowedClipsFragment()
        }
        return fragment.apply { arguments = requireArguments() }
    }
}