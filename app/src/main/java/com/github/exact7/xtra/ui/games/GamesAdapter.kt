package com.github.exact7.xtra.ui.games

import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentGamesListItemBinding
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

class GamesAdapter(
        private val listener: GamesFragment.OnGameSelectedListener) : DataBoundPagedListAdapter<Game, FragmentGamesListItemBinding>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.info.id == newItem.info.id

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.viewers == newItem.viewers
        }) {

    override val itemId: Int
        get() = R.layout.fragment_games_list_item

    override fun bind(binding: FragmentGamesListItemBinding, item: Game?) {
        binding.game = item
        binding.listener = listener
    }
}
