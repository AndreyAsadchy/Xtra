package com.exact.xtra.ui.fragment

open class LazyFragment : androidx.fragment.app.Fragment() {

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
