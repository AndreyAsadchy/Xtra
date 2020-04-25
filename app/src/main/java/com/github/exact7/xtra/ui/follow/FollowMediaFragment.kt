package com.github.exact7.xtra.ui.follow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.exact7.xtra.ui.common.MediaFragment
import com.github.exact7.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosFragment
import kotlinx.android.synthetic.main.fragment_media.*

class FollowMediaFragment : MediaFragment() {

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerFollowed)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = requireContext().getString(R.string.app_name)
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            2 -> FollowedClipsFragment()
            else -> FollowedChannelsFragment()
        }
    }
}