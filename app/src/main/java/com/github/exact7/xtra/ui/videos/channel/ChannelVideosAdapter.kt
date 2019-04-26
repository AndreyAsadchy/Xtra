package com.github.exact7.xtra.ui.videos.channel

import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentChannelVideosListItemBinding
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.download.VideoDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.videos.TempBaseAdapter
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.TwitchApiHelper

class ChannelVideosAdapter(
        private val mainActivity: MainActivity) : TempBaseAdapter<Video, FragmentChannelVideosListItemBinding>(
        object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.preview == newItem.preview &&
                            oldItem.title == newItem.title
        }) {

    //    lateinit var lastSelectedItem: Clip
//        private set

    override val itemId: Int = R.layout.fragment_channel_videos_list_item

    override fun bind(binding: FragmentChannelVideosListItemBinding, item: Video?) {
        binding.video = item
        binding.videoListener = mainActivity
        val activity = binding.root.context as MainActivity
        val showDialog = {
            lastSelectedItem = item!!
            if (DownloadUtils.hasInternalStoragePermission(activity)) {
                VideoDownloadDialog.newInstance(video = item).show(activity.supportFragmentManager, null)
            }
        }
        binding.options.setOnClickListener {
            PopupMenu(activity, binding.options).apply {
                inflate(R.menu.media_item)
                setOnMenuItemClickListener {
                    showDialog.invoke()
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
        binding.root.setOnLongClickListener {
            showDialog.invoke()
            true
        }
        item?.views?.let {
            binding.views.text = binding.views.resources.getQuantityString(R.plurals.views, it, TwitchApiHelper.formatCount(it))
        }
    }
}