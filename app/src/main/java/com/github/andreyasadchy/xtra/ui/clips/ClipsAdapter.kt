package com.github.andreyasadchy.xtra.ui.clips

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.fragment_clips_list_item.view.*

class ClipsAdapter(
        private val fragment: Fragment,
        private val clickListener: BaseClipsFragment.OnClipSelectedListener,
        private val channelClickListener: OnChannelSelectedListener,
        private val showDownloadDialog: (Clip) -> Unit) : BasePagedListAdapter<Clip>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.slug == newItem.slug

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.title == newItem.title

        }) {

    override val layoutId: Int = R.layout.fragment_clips_list_item

    override fun bind(item: Clip, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.broadcaster) }
        with(view) {
            setOnClickListener { clickListener.startClip(item) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.thumbnails.medium, diskCacheStrategy = DiskCacheStrategy.NONE)
            date.text = TwitchApiHelper.formatTime(context, item.createdAt)
            views.text = TwitchApiHelper.formatViewsCount(context, item.views)
            duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
            userImage.apply {
                loadImage(fragment, item.broadcaster.logo, circle = true)
                setOnClickListener(channelListener)
            }
            title.text = item.title
            username.apply {
                setOnClickListener(channelListener)
                text = item.broadcaster.displayName
            }
            gameName.text = item.game
            options.setOnClickListener {
                PopupMenu(context, options).apply {
                    inflate(R.menu.media_item)
                    setOnMenuItemClickListener {
                        showDownloadDialog(item)
                        return@setOnMenuItemClickListener true
                    }
                    show()
                }
            }
        }
    }
}