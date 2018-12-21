package com.github.exact7.xtra.ui.pagers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import kotlinx.android.synthetic.main.view_follow_not_logged.*
import javax.inject.Inject

class FollowPagerFragment : MediaPagerFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel
    var isLoggedIn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        isLoggedIn = viewModel.user.value != null
        return if (isLoggedIn) {
            super.onCreateView(inflater, container, savedInstanceState)
        } else {
            inflater.inflate(R.layout.view_follow_not_logged, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isLoggedIn) {
            super.onViewCreated(view, savedInstanceState)
            setAdapter(FollowPagerAdapter(requireContext(), childFragmentManager))
        } else {
            val activity = requireActivity()
            button.setOnClickListener { activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1) }
            superOnViewCreated(view, savedInstanceState)
        }
    }

    override fun scrollToTop() {
        if (isLoggedIn)
            super.scrollToTop()
    }
}
