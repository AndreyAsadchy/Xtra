package com.exact.twitch.ui.pagers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.exact.twitch.util.C
import com.exact.twitch.util.TwitchApiHelper

class FollowPagerFragment : MediaPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(FollowPagerAdapter(requireActivity(), childFragmentManager, bundleOf(C.USER to TwitchApiHelper.getUserData(requireActivity()))))
    }
}
