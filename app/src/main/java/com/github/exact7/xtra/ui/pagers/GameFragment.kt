package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.clips.common.ClipsFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.streams.common.StreamsFragment
import com.github.exact7.xtra.ui.videos.game.GameVideosFragment
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.fragment_media.*


class GameFragment : MediaFragment() {

    companion object {
        fun newInstance(game: Game) = GameFragment().apply { arguments = bundleOf(C.GAME to game) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        toolbar.apply {
            title = requireArguments().getParcelable<Game>(C.GAME)!!.info.name
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> StreamsFragment()
            1 -> GameVideosFragment()
            else -> ClipsFragment()
        }
        return fragment.also { it.arguments = requireArguments() }
    }
}