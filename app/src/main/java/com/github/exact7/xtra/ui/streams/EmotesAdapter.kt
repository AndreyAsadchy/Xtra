package com.github.exact7.xtra.ui.streams

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.TwitchEmote
import com.github.exact7.xtra.ui.common.BTTV_URL
import com.github.exact7.xtra.ui.common.EMOTES_URL
import com.github.exact7.xtra.util.DisplayUtils
import com.github.exact7.xtra.util.loadImage

class EmotesAdapter(
        context: Context,
        private val list: List<Emote>,
        private val clickListener: (Emote) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val size = DisplayUtils.convertDpToPixels(context, 40f)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val imageView = ImageView(parent.context).apply {
            updateLayoutParams {
                width = size
                height = size
            }
        }
        return object : RecyclerView.ViewHolder(imageView) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val emote = list[position]
        val url = when (emote) {
            is TwitchEmote -> "$EMOTES_URL${emote.name}/2.0"
            is BttvEmote -> "$BTTV_URL${emote.id}/2x"
            is FfzEmote -> emote.url
            else -> throw IllegalStateException("Unknown emote")
        }
        (holder.itemView as ImageView).apply {
            loadImage(url, diskCacheStrategy = DiskCacheStrategy.RESOURCE)
            setOnClickListener { clickListener(emote) }
        }
    }

    override fun getItemCount(): Int = list.size
}