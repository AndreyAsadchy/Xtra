package com.github.exact7.xtra.ui.pagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.top.TopVideosFragment

class TopPagerAdapter(context: Context, fm: FragmentManager) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> StreamsFragment()
            1 -> TopVideosFragment()
            else -> ClipsFragment()
        }
    }
}
