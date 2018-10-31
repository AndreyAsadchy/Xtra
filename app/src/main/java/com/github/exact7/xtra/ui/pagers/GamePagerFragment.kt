package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.github.exact7.xtra.model.game.Game

class GamePagerFragment : MediaPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(GamePagerAdapter(requireActivity(), childFragmentManager, arguments!!))
    }

    companion object {
        fun newInstance(game: Game) = GamePagerFragment().apply { arguments = bundleOf("game" to game) }
    }
}
