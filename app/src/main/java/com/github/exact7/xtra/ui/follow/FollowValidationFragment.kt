package com.github.exact7.xtra.ui.follow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_follow.*
import kotlinx.android.synthetic.main.fragment_follow.view.*
import javax.inject.Inject

class FollowValidationFragment : Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var mediaFragment: FollowMediaFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val viewModel = ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java)
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user !is NotLoggedIn) {
                mediaFragment = childFragmentManager.findFragmentById(R.id.container) as FollowMediaFragment? ?: FollowMediaFragment().also { childFragmentManager.beginTransaction().replace(R.id.container, it).commit() }
            } else {
                notLoggedInLayout.visible()
                notLoggedInLayout.search.setOnClickListener { activity.openSearch() }
                notLoggedInLayout.login.setOnClickListener { activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1) }
            }
        })
    }

    override fun scrollToTop() {
        mediaFragment?.scrollToTop()
    }
}