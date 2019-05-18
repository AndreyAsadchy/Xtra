package com.github.exact7.xtra.ui.common.pagers

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

abstract class ItemAwareFragmentPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    internal var currentFragment: Fragment? = null
        private set

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        currentFragment = `object` as Fragment
    }
}
