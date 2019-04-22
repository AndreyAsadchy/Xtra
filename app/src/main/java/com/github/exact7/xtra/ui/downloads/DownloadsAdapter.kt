package com.github.exact7.xtra.ui.downloads

import android.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.FragmentDownloadsListItemBinding
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.common.DataBoundViewHolder

class DownloadsAdapter(
        private val clickCallback: DownloadsFragment.OnVideoSelectedListener,
        private val deleteCallback: (OfflineVideo) -> Unit) : ListAdapter<OfflineVideo, DataBoundViewHolder<FragmentDownloadsListItemBinding>>(
        object : DiffUtil.ItemCallback<OfflineVideo>() {
            override fun areItemsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return false //bug, oldItem and newItem are sometimes the same
            }
        }) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<FragmentDownloadsListItemBinding> {
        return DataBoundViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.fragment_downloads_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<FragmentDownloadsListItemBinding>, position: Int) {
        val binding = holder.binding
        val context = binding.date.context
        val item = getItem(position)
        val delete = context.getString(R.string.delete)
        val dialog = AlertDialog.Builder(context)
                .setTitle(delete)
                .setMessage(context.getString(R.string.are_you_sure))
                .setPositiveButton(delete) { _, _ -> deleteCallback.invoke(item) }
                .setNegativeButton(context.getString(android.R.string.cancel), null)
        binding.video = item
        binding.root.setOnClickListener { clickCallback.startOfflineVideo(item) }
        binding.root.setOnLongClickListener {
            dialog.show()
            true
        }
        binding.downloading.setOnClickListener { dialog.show() }
        binding.downloading.setOnLongClickListener {
            dialog.show()
            true
        }
        binding.options.setOnClickListener {
            PopupMenu(context, binding.options).apply {
                inflate(R.menu.offline_item)
                setOnMenuItemClickListener {
                    dialog.show()
                    true
                }
                show()
            }
        }
        binding.progressBar.progress = (item.lastWatchPosition.toFloat() / item.duration * 100).toInt()
        item.sourceStartPosition?.let {
            binding.sourceStart.text = context.getString(R.string.source_vod_start, DateUtils.formatElapsedTime(it / 1000L))
            binding.sourceEnd.text = context.getString(R.string.source_vod_end, DateUtils.formatElapsedTime((it + item.duration) / 1000L))
        }
    }
}