package com.github.exact7.xtra.ui.common.follow

import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.util.FragmentUtils
import com.github.exact7.xtra.util.visible

interface FollowFragment { //TODO REFACTOR
    fun initializeFollow(fragment: Fragment, viewModel: FollowViewModel, followButton: ImageButton, user: LoggedIn) {
        val context = fragment.requireContext()
        with(viewModel) {
            setUser(user)
            followButton.visible()
            var initialized = false
            val channelName = channelInfo.second
            follow.observe(fragment.viewLifecycleOwner, Observer { following ->
                if (initialized) {
                    Toast.makeText(context, context.getString(if (following) R.string.now_following else R.string.unfollowed, channelName), Toast.LENGTH_SHORT).show()
                } else {
                    initialized = true
                }
                followButton.setOnClickListener {
                    if (!following) {
                        follow.value = true
                    } else {
                        FragmentUtils.showUnfollowDialog(context, channelName) { follow.value = false }
                    }
                }
                followButton.setImageResource(if (following) R.drawable.baseline_favorite_black_24 else R.drawable.baseline_favorite_border_black_24)
            })
        }
    }
}