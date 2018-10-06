package com.exact.xtra.ui.videos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.exact.xtra.R
import com.exact.xtra.databinding.FragmentVideosListItemBinding
import com.exact.xtra.model.video.Video
import com.exact.xtra.ui.DataBoundPagedListAdapter

class VideosAdapter(
        private val clickCallback: BaseVideosFragment.OnVideoSelectedListener) : DataBoundPagedListAdapter<Video, FragmentVideosListItemBinding>(
        object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.preview == newItem.preview &&
                            oldItem.title == newItem.title
        }) {

    override fun createBinding(parent: ViewGroup): FragmentVideosListItemBinding {
        val binding = DataBindingUtil.inflate<FragmentVideosListItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.fragment_videos_list_item,
                parent,
                false
        )
        binding.root.setOnClickListener { binding.video?.let(clickCallback::startVideo) }
        return binding
    }

    override fun bind(binding: FragmentVideosListItemBinding, item: Video?) {
        binding.video = item
    }
}