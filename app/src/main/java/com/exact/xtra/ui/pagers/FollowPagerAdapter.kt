package com.exact.xtra.ui.pagers

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.exact.xtra.ui.clips.FollowedClipsFragment
import com.exact.xtra.ui.streams.FollowedStreamsFragment
import com.exact.xtra.ui.videos.FollowedVideosFragment

class FollowPagerAdapter(context: Context, fm: FragmentManager, private val args: Bundle) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): Fragment? {
        val fragment: Fragment? = when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            2 -> FollowedClipsFragment()
            else -> null
        }
        return fragment?.apply { arguments = args }
    }
}
