package com.github.exact7.xtra.ui.pagers

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.game.GameVideosFragment

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
