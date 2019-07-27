package com.github.exact7.xtra.ui.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.exact7.xtra.ui.search.channels.ChannelSearchFragment

class SearchPagerAdapter(
        private val context: Context,
        fm: FragmentManager) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        val id = when (position) {
            0 -> R.string.channels
            else -> R.string.games
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ChannelSearchFragment()
            else -> ChannelSearchFragment()
        }
    }

    override fun getCount(): Int = 2
}
