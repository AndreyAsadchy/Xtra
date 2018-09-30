package com.exact.twitch.ui.clips

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.exact.twitch.R
import com.exact.twitch.databinding.FragmentClipsListItemBinding
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.ui.DataBoundPagedListAdapter

class ClipsAdapter(
        private val clickCallback: BaseClipsFragment.OnClipSelectedListener) : DataBoundPagedListAdapter<Clip, FragmentClipsListItemBinding>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.slug == newItem.slug

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.title == newItem.title

        }) {

    override fun createBinding(parent: ViewGroup): FragmentClipsListItemBinding {
        val binding = DataBindingUtil.inflate<FragmentClipsListItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.fragment_clips_list_item,
                parent,
                false
        )
        binding.root.setOnClickListener { binding.clip?.let(clickCallback::startClip) }
        return binding
    }

    override fun bind(binding: FragmentClipsListItemBinding, item: Clip?) {
        binding.clip = item
    }
}
