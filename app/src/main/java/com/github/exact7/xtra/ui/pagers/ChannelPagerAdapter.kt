package com.github.exact7.xtra.ui.pagers

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.videos.channel.ChannelVideosFragment

class ChannelPagerAdapter(
        private val context: Context,
        fm: FragmentManager,
        private val args: Bundle) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        val id = when (position) {
            0 -> R.string.videos
            else -> R.string.clips
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> ChannelVideosFragment()
            else -> ClipsFragment()
        }
        return fragment.apply { arguments = args }
    }

    override fun getCount(): Int = 2
}
