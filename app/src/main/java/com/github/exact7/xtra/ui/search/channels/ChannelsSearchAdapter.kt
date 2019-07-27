package com.github.exact7.xtra.ui.search.channels

import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentSearchChannelsListItemBinding
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.ui.common.DataBoundPagedListAdapter
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener

class ChannelsSearchAdapter(
        private val listener: OnChannelSelectedListener) : DataBoundPagedListAdapter<Channel, FragmentSearchChannelsListItemBinding>(
        object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean = true
        }) {

    override val itemId: Int
        get() =  R.layout.fragment_search_channels_list_item

    override fun bind(binding: FragmentSearchChannelsListItemBinding, item: Channel?) {
        binding.channel = item
        binding.listener = listener
    }
}
