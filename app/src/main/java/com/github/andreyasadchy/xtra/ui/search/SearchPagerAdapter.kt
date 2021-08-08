package com.github.andreyasadchy.xtra.ui.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchFragment
import com.github.andreyasadchy.xtra.ui.search.games.GameSearchFragment

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
            else -> GameSearchFragment()
        }
    }

    override fun getCount(): Int = 2
}
