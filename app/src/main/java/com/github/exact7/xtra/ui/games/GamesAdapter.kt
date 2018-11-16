package com.github.exact7.xtra.ui.games

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentGamesListItemBinding
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

class GamesAdapter(
        private val clickCallback: (Game) -> Unit) : DataBoundPagedListAdapter<Game, FragmentGamesListItemBinding>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.info.id == newItem.info.id

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.viewers == newItem.viewers
        }) {

    override fun itemId(parent: ViewGroup): FragmentGamesListItemBinding {
        val binding = DataBindingUtil.inflate<FragmentGamesListItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.fragment_games_list_item,
                parent,
                false
        )
        binding.root.setOnClickListener { binding.game?.let(clickCallback::invoke) }
        return binding
    }

    override fun bind(binding: FragmentGamesListItemBinding, item: Game?) {
        binding.game = item
    }
}
