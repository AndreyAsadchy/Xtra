package com.github.exact7.xtra.ui.common

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.fragment.LazyFragment
import com.github.exact7.xtra.ui.main.MainViewModel
import javax.inject.Inject

abstract class BaseNetworkFragment : LazyFragment(), Injectable {

    private companion object {
        const val INITIALIZED_KEY = "isInitialized"
    }

    @Inject protected lateinit var viewModelFactory: ViewModelProvider.Factory
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isInitialized = it.getBoolean(INITIALIZED_KEY)
        }
        val viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.isNetworkAvailable().observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!isInitialized) {
                    initialize()
                    isInitialized = true
                } else {
                    onNetworkRestored()
                }
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INITIALIZED_KEY, isInitialized)
    }

    abstract fun initialize()
    abstract fun onNetworkRestored()
}