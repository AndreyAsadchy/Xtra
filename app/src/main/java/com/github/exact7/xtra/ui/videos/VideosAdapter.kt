package com.github.exact7.xtra.ui.videos

import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentVideosListItemBinding
import com.github.exact7.xtra.model.video.Video
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

class VideosAdapter(
        private val listener: BaseVideosFragment.OnVideoSelectedListener) : DataBoundPagedListAdapter<Video, FragmentVideosListItemBinding>(
        object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.preview == newItem.preview &&
                            oldItem.title == newItem.title
        }) {

    override val itemId: Int
        get() = R.layout.fragment_videos_list_item

    override fun bind(binding: FragmentVideosListItemBinding, item: Video?) {
        binding.video = item
        binding.listener = listener
    }
}