package com.github.exact7.xtra.ui.games

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class GamesAdapter(
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
            gameImage.loadImage(item.game.box.medium)
            gameName.text = item.game.name
            viewers.text = if (item.viewers > 1000) {
                resources.getString(R.string.viewers, TwitchApiHelper.formatCount(item.viewers))
            } else {
                resources.getQuantityString(R.plurals.viewers, item.viewers, item.viewers)
            }
        }
    }
}
