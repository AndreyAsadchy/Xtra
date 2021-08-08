package com.github.andreyasadchy.xtra.ui.games

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.game.GameWrapper
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class GamesAdapter(
        private val fragment: Fragment,
        private val listener: GamesFragment.OnGameSelectedListener) : BasePagedListAdapter<GameWrapper>(
        object : DiffUtil.ItemCallback<GameWrapper>() {
            override fun areItemsTheSame(oldItem: GameWrapper, newItem: GameWrapper): Boolean =
                    oldItem.game.id == newItem.game.id

            override fun areContentsTheSame(oldItem: GameWrapper, newItem: GameWrapper): Boolean =
                    oldItem.viewers == newItem.viewers
        }) {

    override val layoutId: Int = R.layout.fragment_games_list_item

    override fun bind(item: GameWrapper, view: View) {
        with(view) {
            setOnClickListener { listener.openGame(item.game) }
            gameImage.loadImage(fragment, item.game.box.medium)
            gameName.text = item.game.name
            viewers.text = TwitchApiHelper.formatViewersCount(context, item.viewers)
        }
    }
}
