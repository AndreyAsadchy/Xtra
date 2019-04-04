package com.github.exact7.xtra.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentFollowBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.pagers.FollowMediaFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_follow.*
import kotlinx.android.synthetic.main.view_follow_not_logged.view.*
import javax.inject.Inject

class FollowValidationFragment : Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentFollowBinding
    private var isLoggedIn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentFollowBinding.inflate(inflater, container, false).let {
            binding = it
            it.lifecycleOwner = viewLifecycleOwner
            binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        val viewModel = ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.user.observe(viewLifecycleOwner, Observer {
            isLoggedIn = it is LoggedIn
            if (isLoggedIn) {
                childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FollowMediaFragment()).commit()
                fragmentContainer.post { fragmentContainer.findViewById<Toolbar>(R.id.toolbar).title = activity.getString(R.string.app_name) }
            } else {
                notLoggedInLayout.loginText.setOnClickListener { activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1) }
            }
        })
    }

    override fun scrollToTop() {
        if (isLoggedIn) {
            recyclerView.scrollToPosition(0)
        }
    }
}