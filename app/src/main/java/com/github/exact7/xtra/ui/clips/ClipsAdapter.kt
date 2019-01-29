package com.github.exact7.xtra.ui.clips

import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentClipsListItemBinding
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.common.DataBoundPagedListAdapter
import com.github.exact7.xtra.ui.download.ClipDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.DownloadUtils

class ClipsAdapter(
        private val mainActivity: MainActivity) : DataBoundPagedListAdapter<Clip, FragmentClipsListItemBinding>(
        object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.slug == newItem.slug

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean =
                    oldItem.views == newItem.views &&
                            oldItem.title == newItem.title

        }) {

    lateinit var lastSelectedItem: Clip
        private set

    override val itemId: Int = R.layout.fragment_clips_list_item

    override fun bind(binding: FragmentClipsListItemBinding, item: Clip?) {
        binding.clip = item
        binding.clipListener = mainActivity
        binding.channelListener = mainActivity
        val showDialog = {
            lastSelectedItem = item!!
            if (DownloadUtils.hasStoragePermission(mainActivity)) {
                ClipDownloadDialog.newInstance(item).show(mainActivity.supportFragmentManager, null)
            }
        }
        binding.options.setOnClickListener {
            PopupMenu(mainActivity, binding.options).apply {
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
    }
}
