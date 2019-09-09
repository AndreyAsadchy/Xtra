package com.github.exact7.xtra.ui.view.chat

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.ui.common.BaseListAdapter
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.*

class AutoCompleteEmotesAdapter(private val clickListener: (Emote) -> Unit) : BaseListAdapter<Emote>(object : DiffUtil.ItemCallback<Emote>() {
    override fun areItemsTheSame(oldItem: Emote, newItem: Emote): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Emote, newItem: Emote): Boolean {
        return true
    }

}) {

    override val layoutId: Int = R.layout.auto_complete_emotes_list_item

    override fun bind(item: Emote, view: View) {
        with(view) {
            setOnClickListener { clickListener(item) }
            image.loadImage(item.url)
            name.text = item.name
        }
    }
}