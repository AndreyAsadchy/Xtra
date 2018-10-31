package com.github.exact7.xtra.ui.clips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentClipsListItemBinding
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

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
        return DataBindingUtil.inflate<FragmentClipsListItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.fragment_clips_list_item,
                parent,
                false).apply { root.setOnClickListener { _ -> clip?.let(clickCallback::startClip) } }
    }

    override fun bind(binding: FragmentClipsListItemBinding, item: Clip?) {
        binding.clip = item
    }
}
