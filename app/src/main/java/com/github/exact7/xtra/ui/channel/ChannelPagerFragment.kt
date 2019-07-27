package com.github.exact7.xtra.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.common.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.convertDpToPixels
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.isInLandscapeOrientation
import com.github.exact7.xtra.util.loadImage
import com.github.exact7.xtra.util.visible
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_media_pager.*


class ChannelPagerFragment : MediaPagerFragment(), Injectable, FollowFragment {

    companion object {
        fun newInstance(channel: Channel) = ChannelPagerFragment().apply { arguments = bundleOf(C.CHANNEL to channel) }
    }

    override lateinit var viewModel: ChannelPagerViewModel
    private lateinit var channel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channel = requireArguments().getParcelable(C.CHANNEL)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(ChannelPagerAdapter(activity, childFragmentManager, requireArguments()))
        if (currentFragment !is ChatFragment) {
            if (activity.isInLandscapeOrientation) {
                appBar.setExpanded(false, false)
            }
        } else {
            appBar.gone()
        }
        collapsingToolbar.title = channel.displayName
        logo.loadImage(channel.logo, circle = true)
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.setOnClickListener { activity.openSearch() }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            private val layoutParams = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
            private val originalScrollFlags = layoutParams.scrollFlags

            override fun onPageSelected(position: Int) {
                layoutParams.scrollFlags = if (position != 2) {
                    originalScrollFlags
                } else {
                    appBar.setExpanded(false, true)
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
    }

    override fun initialize() {
        viewModel = createViewModel()
        viewModel.loadStream(channel)
        val activity = requireActivity() as MainActivity
        viewModel.stream.observe(viewLifecycleOwner, Observer {
            watchLive.visible(it.stream != null)
            it.stream?.let { s ->
                toolbarContainer.updateLayoutParams { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                collapsingToolbar.expandedTitleMarginBottom = activity.convertDpToPixels(50.5f)
                watchLive.setOnClickListener { activity.startStream(s) }
            }
        })
        getMainViewModel().user.observe(viewLifecycleOwner, Observer {
            if (it is LoggedIn) {
                initializeFollow(this, viewModel, follow, it)
            }
        })
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}