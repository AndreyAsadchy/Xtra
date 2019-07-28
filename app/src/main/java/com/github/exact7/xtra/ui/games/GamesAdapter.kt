package com.github.exact7.xtra.ui.games

import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentGamesListItemBinding
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import com.github.exact7.xtra.ui.common.DataBoundPagedListAdapter
import com.github.exact7.xtra.util.TwitchApiHelper

class GamesAdapter(
        private val listener: GamesFragment.OnGameSelectedListener) : DataBoundPagedListAdapter<GameWrapper, FragmentGamesListItemBinding>(
        object : DiffUtil.ItemCallback<GameWrapper>() {
            override fun areItemsTheSame(oldItem: GameWrapper, newItem: GameWrapper): Boolean =
                    oldItem.game.id == newItem.game.id

            override fun areContentsTheSame(oldItem: GameWrapper, newItem: GameWrapper): Boolean =
                    oldItem.viewers == newItem.viewers
        }) {

    override val itemId: Int
        get() = R.layout.fragment_games_list_item

    override fun bind(binding: FragmentGamesListItemBinding, item: GameWrapper?) {
        binding.game = item
        binding.listener = listener
        item?.viewers?.let {
            binding.viewers.text = if (it > 1000) {
                binding.viewers.resources.getString(R.string.viewers, TwitchApiHelper.formatCount(it))
            } else {
                binding.viewers.resources.getQuantityString(R.plurals.viewers, it, it)
            }
        }
    }
}
