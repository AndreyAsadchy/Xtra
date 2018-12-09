package com.github.exact7.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.fragment.LazyFragment
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.NetworkUtils
import javax.inject.Inject

abstract class BaseNetworkFragment : LazyFragment(), Injectable {

    private companion object {
        const val PREVIOUS_KEY = "previous"
    }

    @Inject protected lateinit var viewModelFactory: ViewModelProvider.Factory
    private var previousState = false
    private var shouldRestore = false

    abstract fun initialize()
    abstract fun onNetworkRestored()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousState = savedInstanceState?.getBoolean(PREVIOUS_KEY) ?: NetworkUtils.isConnected(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        val viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner, Observer {
            val isOnline = it.peekContent()
            if (isOnline && !previousState) {
                shouldRestore = if (isFragmentVisible) {
                    onNetworkRestored()
                    false
                } else {
                    true
                }
            }
            previousState = isOnline
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (shouldRestore) {
            onNetworkRestored()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PREVIOUS_KEY, previousState)
    }
}