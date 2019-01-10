package com.github.exact7.xtra.ui.clips

import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentClipsListItemBinding
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.ClipDownloadDialog
import com.github.exact7.xtra.ui.DataBoundPagedListAdapter
import com.github.exact7.xtra.ui.main.MainActivity

class ClipsAdapter(
        private val listener: BaseClipsFragment.OnClipSelectedListener) : DataBoundPagedListAdapter<Clip, FragmentClipsListItemBinding>(
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
        binding.listener = listener
        binding.options.setOnClickListener {
            val context = it.context as MainActivity
            PopupMenu(context, binding.options).apply {
                inflate(R.menu.media_item)
                setOnMenuItemClickListener {
                    ClipDownloadDialog.newInstance(item!!).show(context.supportFragmentManager, null)
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }
}
