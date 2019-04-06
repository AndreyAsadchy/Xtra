package com.github.exact7.xtra.ui.pagers

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentChannelBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DisplayUtils
import kotlinx.android.synthetic.main.fragment_channel.*
import javax.inject.Inject


class ChannelPagerFragment : MediaPagerFragment(), Injectable, FollowFragment {

    companion object {
        fun newInstance(channel: Channel) = ChannelPagerFragment().apply { arguments = bundleOf(C.CHANNEL to channel) }
    }

    private lateinit var binding: FragmentChannelBinding
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentChannelBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(ChannelPagerAdapter(activity, childFragmentManager, requireArguments()))
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChannelPagerViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.loadStream(requireArguments().getParcelable(C.CHANNEL)!!)
        viewModel.stream.observe(viewLifecycleOwner, Observer {
            it.stream?.let { s ->
                toolbarContainer.updateLayoutParams { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                collapsingToolbar.expandedTitleMarginBottom = DisplayUtils.convertDpToPixels(activity, 50.5f)
                watchLive.setOnClickListener { activity.startStream(s) }
            }
        })
        ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java).user.observe(viewLifecycleOwner, Observer {
            if (it is LoggedIn) {
                initializeFollow(this, viewModel, follow, it)
            }
        })
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            appBar.setExpanded(false, false)
        }
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.setOnClickListener { activity.openSearch() }
    }
}