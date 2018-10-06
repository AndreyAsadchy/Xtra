package com.exact.xtra.ui.games

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.exact.xtra.R
import com.exact.xtra.databinding.FragmentGamesListItemBinding
import com.exact.xtra.model.game.Game
import com.exact.xtra.ui.DataBoundPagedListAdapter

class GamesAdapter(
        private val clickCallback: (Game) -> Unit) : DataBoundPagedListAdapter<Game, FragmentGamesListItemBinding>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.info.id == newItem.info.id

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.viewers == newItem.viewers
        }) {

    override fun createBinding(parent: ViewGroup): FragmentGamesListItemBinding {
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
