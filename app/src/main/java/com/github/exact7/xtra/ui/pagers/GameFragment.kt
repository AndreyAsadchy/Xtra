package com.github.exact7.xtra.ui.pagers

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.game.GameVideosFragment
import com.github.exact7.xtra.util.C

class GameFragment : MediaFragment() {

    companion object {
        fun newInstance(game: Game) = GameFragment().apply { arguments = bundleOf(C.GAME to game) }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> StreamsFragment()
            1 -> GameVideosFragment()
            else -> ClipsFragment()
        }
        return fragment.apply { arguments = requireArguments() }
    }
}