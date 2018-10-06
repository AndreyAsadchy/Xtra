package com.exact.xtra.ui.pagers

import android.content.Context
import com.exact.xtra.ui.clips.ClipsFragment
import com.exact.xtra.ui.streams.StreamsFragment
import com.exact.xtra.ui.videos.TopVideosFragment

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
