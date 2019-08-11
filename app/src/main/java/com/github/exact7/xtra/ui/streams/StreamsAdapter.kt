package com.github.exact7.xtra.ui.streams

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.loadImage
import com.github.exact7.xtra.util.setTint
import kotlinx.android.synthetic.main.fragment_streams_list_item.view.*

class StreamsAdapter(
        private val clickListener: BaseStreamsFragment.OnStreamSelectedListener,
        private val channelClickListener: OnChannelSelectedListener) : BasePagedListAdapter<Stream>(
        object : DiffUtil.ItemCallback<Stream>() {
            override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean =
                    oldItem.viewers == newItem.viewers &&
                            oldItem.game == newItem.game &&
                            oldItem.channel.status == newItem.channel.status
        }) {

    override val layoutId: Int = R.layout.fragment_streams_list_item

    override fun bind(item: Stream, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.channel) }
        with(view) {
            setOnClickListener { clickListener.startStream(item) }
            thumbnail.loadImage(item.preview.large, true)
            viewers.text = TwitchApiHelper.formatCount(context, item.viewers, true)
            streamTypeImage.apply {
                val image: Int
                val tint: Int
                if (item.streamType == StreamType.LIVE) {
                    image = R.drawable.baseline_fiber_manual_record_black_24
                    tint = android.R.color.holo_red_dark
                } else {
                    image = R.drawable.baseline_replay_black_24
                    tint = android.R.color.darker_gray
                }
                setImageResource(image)
                setTint(tint)
            }
            userImage.apply {
                setOnClickListener(channelListener)
                loadImage(item.channel.logo, circle = true)
            }
            username.apply {
                setOnClickListener(channelListener)
                text = item.channel.displayName
            }
            title.text = item.channel.status.trim()
            gameName.text = item.game
        }
    }
}
