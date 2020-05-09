package com.github.exact7.xtra.ui.videos.channel

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.videos.BaseVideosAdapter
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.loadImage
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class ChannelVideosAdapter(
        private val clickListener: BaseVideosFragment.OnVideoSelectedListener,
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

    override val layoutId: Int = R.layout.fragment_channel_videos_list_item

    override fun bind(item: Video, view: View) {
        with(view) {
            val position = positions?.get(item.id.substring(1).toLong())
            setOnClickListener { clickListener.startVideo(item, position?.toDouble()) }
            setOnLongClickListener { showDownloadDialog(item); true }
            thumbnail.loadImage(item.preview.large)
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