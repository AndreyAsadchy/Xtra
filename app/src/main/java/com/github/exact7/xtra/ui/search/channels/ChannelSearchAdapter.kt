package com.github.exact7.xtra.ui.search.channels

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_search_channels_list_item.view.*

class ChannelSearchAdapter(
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
            logo.loadImage(item.logo)
            name.text = item.displayName
        }
    }
}