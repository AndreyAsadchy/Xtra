package com.exact.twitch.ui.pagers

import android.os.Bundle
import android.view.View
import com.exact.twitch.util.C
import com.exact.twitch.util.TwitchApiHelper

class FollowPagerFragment : MediaPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = Bundle(1)
        args.putString(C.TOKEN, TwitchApiHelper.getUserToken(requireActivity()))
        setAdapter(FollowPagerAdapter(requireActivity(), childFragmentManager, args))
    }
}
