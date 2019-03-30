package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.databinding.FragmentFollowBinding
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosFragment
import com.github.exact7.xtra.util.C

class FollowFragment : MediaFragment() {

    companion object {
        fun newInstance(user: User) = FollowFragment().apply { arguments = bundleOf(C.USER to user) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentFollowBinding.inflate(inflater, container, false).let {
            binding = it
            it.lifecycleOwner = viewLifecycleOwner
            binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user is LoggedIn) {

        }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedVideosFragment()
            else -> FollowedClipsFragment()
        }
        return fragment.also { it.arguments = requireArguments() }
    }
}