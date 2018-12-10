package com.github.exact7.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.NetworkUtils
import javax.inject.Inject

abstract class BaseNetworkFragment : Fragment(), Injectable {

    private companion object {
        const val LAST_KEY = "last"
        const val RESTORE_KEY = "restore"
        const val CREATED_KEY = "created"
    }

    @Inject protected lateinit var viewModelFactory: ViewModelProvider.Factory
    private var lastState = false
    private var shouldRestore = false
    private var isInitialized = false
    private var created = false

    abstract fun initialize()
    abstract fun onNetworkRestored()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastState = savedInstanceState?.getBoolean(LAST_KEY) ?: NetworkUtils.isConnected(requireContext())
        shouldRestore = savedInstanceState?.getBoolean(RESTORE_KEY) ?: false
        created = savedInstanceState?.getBoolean(CREATED_KEY) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isInitialized && (created || (lastState && userVisibleHint))) {
            init()
        }
        val viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner, Observer {
            val isOnline = it.peekContent()
            if (isOnline && !lastState) {
                shouldRestore = if (userVisibleHint) {
                    if (isInitialized) {
                        onNetworkRestored()
                    } else {
                        init()
                    }
                    false
                } else {
                    true
                }
            }
            lastState = isOnline
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInitialized = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isInitialized) {
            if (isVisibleToUser && isResumed && lastState) {
                init()
            }
        } else if (shouldRestore && lastState) {
            if (isVisibleToUser) {
                onNetworkRestored()
                shouldRestore = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(LAST_KEY, lastState)
        outState.putBoolean(RESTORE_KEY, shouldRestore)
        outState.putBoolean(CREATED_KEY, created)
    }

    private fun init() {
        initialize()
        isInitialized = true
        created = true
    }
}