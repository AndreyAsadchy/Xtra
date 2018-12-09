package com.github.exact7.xtra.ui.fragment

import androidx.fragment.app.Fragment

abstract class LazyFragment : Fragment() {

    var isFragmentVisible: Boolean = false
        private set
    private var loaded: Boolean = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = if (isVisibleToUser) {
            if (!loaded) {
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
                loaded = true
            }
            true
        } else {
            false
        }
    }
}
