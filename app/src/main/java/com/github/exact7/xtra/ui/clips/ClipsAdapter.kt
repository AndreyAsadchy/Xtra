package com.github.exact7.xtra.ui.clips

import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentClipsListItemBinding
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter

class ClipsAdapter : DataBoundPagedListAdapter<Clip, FragmentClipsListItemBinding>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.slug == newItem.slug

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.title == newItem.title

        }) {


    override val itemId: Int
        get() = R.layout.fragment_clips_list_item

    override fun bind(binding: FragmentClipsListItemBinding, item: Clip?) {
        binding.clip = item
    }
}
