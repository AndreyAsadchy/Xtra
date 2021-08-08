package com.github.andreyasadchy.xtra.ui.clips.common

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_channel_clips_list_item.view.*

class ChannelClipsAdapter(
        private val fragment: Fragment,
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
            thumbnail.loadImage(fragment, item.thumbnails.medium, diskCacheStrategy = DiskCacheStrategy.NONE)
            date.text = TwitchApiHelper.formatTime(context, item.createdAt)
            views.text = TwitchApiHelper.formatViewsCount(context, item.views)
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
