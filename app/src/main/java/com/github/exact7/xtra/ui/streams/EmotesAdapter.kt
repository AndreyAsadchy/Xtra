package com.github.exact7.xtra.ui.streams

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.common.BTTV_URL
import com.github.exact7.xtra.ui.common.EMOTES_URL
import com.github.exact7.xtra.util.loadImage

class EmotesAdapter(
        private val list: List<Emote>,
        private val clickListener: (Emote) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_emotes_list_item, parent, false)) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val emote = list[position]
        val url = when (emote) {
            is BttvEmote -> "$BTTV_URL${emote.id}/2x"
            is FfzEmote -> emote.url
            is com.github.exact7.xtra.model.kraken.user.Emote -> "$EMOTES_URL${emote.id}/2.0"
            else -> throw IllegalStateException("Unknown emote")
        }
        (holder.itemView as ImageView).apply {
            loadImage(url)
            setOnClickListener { clickListener(emote) }
        }
    }

    override fun getItemCount(): Int = list.size
}