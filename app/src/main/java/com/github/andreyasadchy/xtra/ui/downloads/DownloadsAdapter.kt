package com.github.andreyasadchy.xtra.ui.downloads

import android.annotation.SuppressLint
import android.os.Parcel
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.BaseListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.*
import kotlinx.coroutines.channels.Channel

class DownloadsAdapter(
        private val fragment: Fragment,
        private val clickListener: DownloadsFragment.OnVideoSelectedListener,
        private val deleteVideo: (OfflineVideo) -> Unit
       ) : BaseListAdapter<OfflineVideo>(
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
            val onClick = View.OnClickListener{
                v ->
                if(v.id == userImage.id || v.id == username.id){
                    //TODO Open up the channel/profile viewer
                }else{
                    clickListener.startOfflineVideo(item)
                }
            }
            userImage.setOnClickListener(onClick)
            username.setOnClickListener(onClick)
            setOnClickListener(onClick)
            setOnLongClickListener { deleteVideo(item); true }
            thumbnail.loadImage(fragment, item.thumbnail, diskCacheStrategy = DiskCacheStrategy.AUTOMATIC)
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
                sourceStart.text = context.getString(R.string.source_vod_start, DateUtils.formatElapsedTime(it / 1000L))
                sourceEnd.text = context.getString(R.string.source_vod_end, DateUtils.formatElapsedTime((it + item.duration) / 1000L))
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