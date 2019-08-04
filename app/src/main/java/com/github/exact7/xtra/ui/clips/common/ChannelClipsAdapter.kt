package com.github.exact7.xtra.ui.clips.common

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.clips.BaseClipsFragment
import com.github.exact7.xtra.ui.common.BasePagedListAdapter
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_channel_clips_list_item.view.*

class ChannelClipsAdapter(
        private val clickListener: BaseClipsFragment.OnClipSelectedListener,
        private val showDownloadDialog: (Clip) -> Unit) : BasePagedListAdapter<Clip>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.slug == newItem.slug

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.title == newItem.title

        }) {

    override val layoutId: Int = R.layout.fragment_channel_clips_list_item

    override fun bind(item: Clip, view: View) {
        with(view) {
            setOnClickListener { clickListener.startClip(item) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(item.thumbnails.medium)
            date.text = TwitchApiHelper.formatTime(context, item.createdAt)
            views.text = TwitchApiHelper.formatCount(context, item.views)
            duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
            title.text = item.title
            gameName.text = item.game
            options.setOnClickListener {
                PopupMenu(context, it).apply {
                    inflate(R.menu.media_item)
                    setOnMenuItemClickListener { showDownloadDialog(item); true }
                    show()
                }
            }
        }
    }
}
