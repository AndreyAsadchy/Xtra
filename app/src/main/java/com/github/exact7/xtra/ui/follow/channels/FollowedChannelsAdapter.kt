package com.github.exact7.xtra.ui.follow.channels

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.follows.Follow
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_search_channels_list_item.view.*

class FollowedChannelsAdapter(
        private val listener: OnChannelSelectedListener) : BasePagedListAdapter<Follow>(
        object : DiffUtil.ItemCallback<Follow>() {
            override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean =
                    oldItem.channel.id == newItem.channel.id

            override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_followed_channels_list_item

    override fun bind(item: Follow, view: View) {
        with(view) {
            setOnClickListener { listener.viewChannel(item.channel) }
            logo.loadImage(item.channel.logo)
            name.text = item.channel.displayName
        }
    }
}