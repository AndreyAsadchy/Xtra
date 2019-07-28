package com.github.exact7.xtra.ui.search.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.games.GamesFragment
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_search_games_list_item.view.*

class GameSearchAdapter(private val listener: GamesFragment.OnGameSelectedListener) : ListAdapter<Game, GameSearchAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return true
            }
        }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_search_games_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(game: Game) {
            with(containerView) {
                setOnClickListener { listener.openGame(game) }
                logo.loadImage(game.logo.medium)
                name.text = game.name
            }
        }
    }
}


