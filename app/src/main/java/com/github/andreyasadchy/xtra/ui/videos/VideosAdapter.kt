package com.github.andreyasadchy.xtra.ui.videos

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class VideosAdapter(
        private val fragment: Fragment,
        private val clickListener: BaseVideosFragment.OnVideoSelectedListener,
        private val channelClickListener: OnChannelSelectedListener,
        private val showDownloadDialog: (Video) -> Unit) : BaseVideosAdapter(
        object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.preview == newItem.preview &&
                            oldItem.title == newItem.title &&
                            oldItem.length == newItem.length
        }) {

    override val layoutId: Int = R.layout.fragment_videos_list_item

    override fun bind(item: Video, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.channel) }
        with(view) {
            val position = positions?.get(item.id.substring(1).toLong())
            setOnClickListener { clickListener.startVideo(item, position?.toDouble()) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(fragment, item.preview.large, diskCacheStrategy = DiskCacheStrategy.NONE)
            date.text = TwitchApiHelper.formatTime(context, item.createdAt)
            views.text = TwitchApiHelper.formatViewsCount(context, item.views)
            duration.text = DateUtils.formatElapsedTime(item.length.toLong())
            position.let {
                if (it != null) {
                    progressBar.progress = (it / (item.length * 10L)).toInt()
                    progressBar.visible()

                } else {
                    progressBar.gone()
                }
            }
            userImage.apply {
                setOnClickListener(channelListener)
                loadImage(fragment, item.channel.logo, circle = true)
            }
            username.apply {
                setOnClickListener(channelListener)
                text = item.channel.displayName
            }
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