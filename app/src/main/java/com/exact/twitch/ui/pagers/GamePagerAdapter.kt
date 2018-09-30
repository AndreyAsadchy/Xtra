package com.exact.twitch.ui.pagers

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.exact.twitch.ui.clips.ClipsFragment
import com.exact.twitch.ui.streams.StreamsFragment
import com.exact.twitch.ui.videos.GameVideosFragment

class GamePagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager, private val args: Bundle) : MediaPagerAdapter(context, fm) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment? {
        val fragment: androidx.fragment.app.Fragment? = when (position) {
            0 -> StreamsFragment()
            1 -> GameVideosFragment()
            2 -> ClipsFragment()
            else -> null
        }
        return fragment?.apply { arguments = args }
    }
}
