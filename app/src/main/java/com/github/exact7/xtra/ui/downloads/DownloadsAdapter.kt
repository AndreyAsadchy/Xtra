package com.github.exact7.xtra.ui.downloads

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.common.BaseListAdapter
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.loadImage
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.*

class DownloadsAdapter(
        private val fragment: Fragment,
        private val clickListener: DownloadsFragment.OnVideoSelectedListener,
        private val deleteVideo: (OfflineVideo) -> Unit) : BaseListAdapter<OfflineVideo>(
        object : DiffUtil.ItemCallback<OfflineVideo>() {
            override fun areItemsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return false //bug, oldItem and newItem are sometimes the same
            }
        }) {

    override val layoutId: Int = R.layout.fragment_downloads_list_item

    override fun bind(item: OfflineVideo, view: View) {
        with(view) {
            setOnClickListener { clickListener.startOfflineVideo(item) }
            setOnLongClickListener { deleteVideo(item); true }
            thumbnail.loadImage(fragment, item.thumbnail)
            date.text = context.getString(R.string.uploaded_date, TwitchApiHelper.formatTime(context, item.uploadDate))
            downloadDate.text = context.getString(R.string.downloaded_date, TwitchApiHelper.formatTime(context, item.downloadDate))
            duration.text = DateUtils.formatElapsedTime(item.duration / 1000L)
            userImage.loadImage(fragment, item.channelLogo, circle = true)
            title.text = item.name
            username.text = item.channelName
            game.text = item.game
            options.setOnClickListener {
                PopupMenu(context, it).apply {
                    inflate(R.menu.offline_item)
                    setOnMenuItemClickListener { deleteVideo(item); true }
                    show()
                }
            }
            progressBar.progress = (item.lastWatchPosition.toFloat() / item.duration * 100).toInt()
            item.sourceStartPosition?.let {
                sourceStart.text = DateUtils.formatElapsedTime(it / 1000L)
                sourceEnd.text = DateUtils.formatElapsedTime((it + item.duration) / 1000L)
            }
            status.apply {
                if (item.status == OfflineVideo.STATUS_DOWNLOADED) {
                    gone()
                } else {
                    text = if (item.status == OfflineVideo.STATUS_DOWNLOADING) {
                        context.getString(R.string.downloading_progress, ((item.progress.toFloat() / item.maxProgress) * 100f).toInt())
                    } else {
                        context.getString(R.string.download_pending)
                    }
                    visible()
                    setOnClickListener { deleteVideo(item) }
                    setOnLongClickListener { deleteVideo(item); true }
                }
            }
        }
    }
}