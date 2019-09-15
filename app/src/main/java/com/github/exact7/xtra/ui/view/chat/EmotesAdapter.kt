package com.github.exact7.xtra.ui.view.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.util.isActivityResumed
import com.github.exact7.xtra.util.loadImage

class EmotesAdapter(
        private val list: List<Emote>,
        private val clickListener: (Emote) -> Unit,
        private val animateGifs: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_emotes_list_item, parent, false)) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val emote = list[position]
        (holder.itemView as ImageView).apply {
            if (animateGifs) {
                loadImage(emote.url)
            } else {
                if (context.isActivityResumed) {
                    try {
                        GlideApp.with(context)
                                .asBitmap()
                                .load(emote.url)
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .into(this)
                    } catch (e: IllegalArgumentException) {
                        Crashlytics.logException(e)
                    }
                }
            }
            setOnClickListener { clickListener(emote) }
        }
    }

    override fun getItemCount(): Int = list.size
}