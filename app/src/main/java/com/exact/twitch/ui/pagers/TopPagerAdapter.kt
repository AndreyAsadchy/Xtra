package com.exact.twitch.ui.pagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.exact.twitch.ui.clips.ClipsFragment
import com.exact.twitch.ui.pagers.MediaPagerAdapter
import com.exact.twitch.ui.streams.StreamsFragment
import com.exact.twitch.ui.videos.TopVideosFragment

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
