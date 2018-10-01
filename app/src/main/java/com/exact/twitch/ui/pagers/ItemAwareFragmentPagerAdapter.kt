package com.exact.twitch.ui.pagers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.ViewGroup

abstract class ItemAwareFragmentPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    internal lateinit var currentFragment: Fragment
        private set

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        currentFragment = `object` as Fragment
    }
}
