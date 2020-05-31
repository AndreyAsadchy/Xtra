package com.github.exact7.xtra.ui.channel

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.exact7.xtra.ui.videos.channel.ChannelVideosFragment
import com.github.exact7.xtra.util.C

class ChannelPagerAdapter(
        private val context: Context,
        fm: FragmentManager,
        private val args: Bundle) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        val id = when (position) {
            0 -> R.string.videos
            1 -> R.string.clips
//            2 -> R.string.info
            else -> R.string.chat
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> ChannelVideosFragment()
            1 -> ClipsFragment()
//            2 -> ChannelInfoFragment()
            else -> return ChatFragment.newInstance(args.getParcelable(C.CHANNEL)!!)
        }
        return fragment.apply { arguments = args }
    }

//    override fun getCount(): Int = 4
    override fun getCount(): Int = 3
}
