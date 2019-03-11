package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.Scrollable

abstract class MediaPagerFragment : Fragment(), ItemAwarePagerFragment, Scrollable {

    private lateinit var adapter: ItemAwareFragmentPagerAdapter
    private lateinit var viewPager: ViewPager

    override val currentFragment: Fragment?
        get() = adapter.currentFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = when {
            this is FollowPagerFragment || this is ChannelPagerFragment -> view.findViewById<View>(R.id.pagerLayout).findViewById(R.id.viewPager)
            else -> view.findViewById(R.id.viewPager)
        }
    }

    protected fun setAdapter(adapter: ItemAwareFragmentPagerAdapter) {
        this.adapter = adapter
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = adapter.count
    }

    override fun scrollToTop() {
        if (currentFragment is Scrollable) {
            (currentFragment as Scrollable).scrollToTop()
        }
    }
}
