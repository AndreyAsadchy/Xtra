package com.github.exact7.xtra.ui.streams

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentStreamsListItemBinding
import com.github.exact7.xtra.model.stream.Stream
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

class StreamsAdapter : DataBoundPagedListAdapter<Stream, FragmentStreamsListItemBinding>(
        object : DiffUtil.ItemCallback<Stream>() {
            override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.viewers == newItem.viewers &&
                            oldItem.game == newItem.game &&
                            oldItem.channel.status == newItem.channel.status
        }) {

    override fun createBinding(parent: ViewGroup): FragmentStreamsListItemBinding =
            DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.fragment_streams_list_item,
                    parent,
                    false
            )

    override fun bind(binding: FragmentStreamsListItemBinding, item: Stream?) {
        binding.stream = item
    }
}
