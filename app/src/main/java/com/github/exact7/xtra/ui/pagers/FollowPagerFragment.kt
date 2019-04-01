package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentFollowBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.ui.main.MainViewModel
import javax.inject.Inject

class FollowPagerFragment : MediaPagerFragment(), Injectable {

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
            if (it !is NotLoggedIn) {
                isLoggedIn = true
                setAdapter(FollowPagerAdapter(activity, childFragmentManager))
            } else {
//                notLoggedInLayout.loginText.setOnClickListener { activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1) }
            }
        })
    }

    override fun scrollToTop() {
        if (isLoggedIn)
            super.scrollToTop()
    }
}
