package com.github.exact7.xtra.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.isNetworkAvailable
import javax.inject.Inject

abstract class BaseNetworkFragment : Fragment(), Injectable {

    private companion object {
        const val LAST_KEY = "last"
        const val RESTORE_KEY = "restore"
        const val CREATED_KEY = "created"
    }

    protected abstract val viewModel: ViewModel
    @Inject protected lateinit var viewModelFactory: ViewModelProvider.Factory
    protected var enableNetworkCheck = true
    private var lastState = false
    private var shouldRestore = false
    private var isInitialized = false
    private var created = false

    abstract fun initialize()

    open fun onNetworkRestored() {
        (viewModel as? PagedListViewModel<*>)?.retry()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (enableNetworkCheck) {
            lastState = savedInstanceState?.getBoolean(LAST_KEY) ?: requireContext().isNetworkAvailable
            shouldRestore = savedInstanceState?.getBoolean(RESTORE_KEY) ?: false
            created = savedInstanceState?.getBoolean(CREATED_KEY) ?: false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (enableNetworkCheck) {
            if (!isInitialized && (created || (lastState && userVisibleHint))) {
                init()
            }
            getMainViewModel().isNetworkAvailable.observe(viewLifecycleOwner, Observer {
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
        } else {
            initialize()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (enableNetworkCheck) {
            isInitialized = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (enableNetworkCheck) {
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (enableNetworkCheck) {
            outState.putBoolean(LAST_KEY, lastState)
            outState.putBoolean(RESTORE_KEY, shouldRestore)
            outState.putBoolean(CREATED_KEY, created)
        }
    }

    protected inline fun <reified T : ViewModel> createViewModel(): T {
        return ViewModelProviders.of(this, viewModelFactory).get(T::class.java)
    }

    protected fun getMainViewModel(): MainViewModel {
        return ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
    }

    private fun init() {
        initialize()
        isInitialized = true
        created = true
    }
}