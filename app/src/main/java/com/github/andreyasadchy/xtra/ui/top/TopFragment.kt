package com.github.andreyasadchy.xtra.ui.top

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.clips.common.ClipsFragment
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.top.TopVideosFragment
import kotlinx.android.synthetic.main.fragment_media.*

class TopFragment : MediaFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = requireContext().getString(R.string.app_name)
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> StreamsFragment()
            1 -> TopVideosFragment()
            else -> ClipsFragment()
        }
    }
}