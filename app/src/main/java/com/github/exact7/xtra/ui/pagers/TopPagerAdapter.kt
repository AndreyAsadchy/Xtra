package com.github.exact7.xtra.ui.pagers

import android.content.Context
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.top.TopVideosFragment

class TopPagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment? {
        return when (position) {
            0 -> StreamsFragment()
            1 -> TopVideosFragment()
            2 -> ClipsFragment()
            else -> null
        }
    }
}
