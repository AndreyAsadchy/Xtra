package com.exact.xtra.ui.pagers

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.exact.xtra.ui.clips.ClipsFragment
import com.exact.xtra.ui.streams.StreamsFragment
import com.exact.xtra.ui.videos.GameVideosFragment

class GamePagerAdapter(context: Context, fm: FragmentManager, private val args: Bundle) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment? {
        val fragment: Fragment? = when (position) {
            0 -> StreamsFragment()
            1 -> GameVideosFragment()
            2 -> ClipsFragment()
            else -> null
        }
        return fragment?.apply { arguments = args }
    }
}
