package com.github.exact7.xtra.ui.search.games

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.common.BaseListAdapter
import com.github.exact7.xtra.ui.games.GamesFragment
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_search_games_list_item.view.*

class GameSearchAdapter(private val listener: GamesFragment.OnGameSelectedListener) : BaseListAdapter<Game>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return true
            }
        }
) {

    override val layoutId: Int = R.layout.fragment_search_games_list_item

    override fun bind(item: Game, view: View) {
        with(view) {
            setOnClickListener { listener.openGame(item) }
            logo.loadImage(item.logo.medium)
            name.text = item.name
        }
    }
}


