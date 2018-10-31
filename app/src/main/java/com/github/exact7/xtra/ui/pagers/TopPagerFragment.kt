package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.View

class TopPagerFragment : MediaPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(TopPagerAdapter(requireActivity(), childFragmentManager))
    }
}
