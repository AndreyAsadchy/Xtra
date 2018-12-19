package com.github.exact7.xtra.ui.pagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosFragment

class FollowPagerAdapter(context: Context, fm: FragmentManager) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            else -> FollowedClipsFragment()
        }
    }
}
