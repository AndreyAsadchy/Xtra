package com.github.andreyasadchy.xtra.ui.search.channels

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.channel.Channel
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_search_channels_list_item.view.*

class ChannelSearchAdapter(
        private val fragment: Fragment,
        private val listener: OnChannelSelectedListener) : BasePagedListAdapter<Channel>(
        object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_search_channels_list_item

    override fun bind(item: Channel, view: View) {
        with(view) {
            setOnClickListener { listener.viewChannel(item) }
            logo.loadImage(fragment, item.logo)
            name.text = item.displayName
        }
    }
}