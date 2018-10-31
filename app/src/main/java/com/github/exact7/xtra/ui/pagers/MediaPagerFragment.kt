package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R

abstract class MediaPagerFragment : androidx.fragment.app.Fragment(), ItemAwarePagerFragment {

    private lateinit var adapter: ItemAwareFragmentPagerAdapter
    private lateinit var viewPager: androidx.viewpager.widget.ViewPager

    override val currentFragment: Fragment
        get() = adapter.currentFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.offscreenPageLimit = 2
    }

    protected fun setAdapter(adapter: ItemAwareFragmentPagerAdapter) {
        this.adapter = adapter
        viewPager.adapter = adapter
    }
}
