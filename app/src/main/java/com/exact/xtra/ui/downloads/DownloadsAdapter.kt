package com.exact.xtra.ui.downloads

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.exact.xtra.R
import com.exact.xtra.databinding.FragmentDownloadsListItemBinding
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.ui.DataBoundViewHolder

class DownloadsAdapter(
        private val clickCallback: DownloadsFragment.OnVideoSelectedListener,
        private val deleteCallback: (OfflineVideo) -> Unit) : ListAdapter<OfflineVideo, DataBoundViewHolder<FragmentDownloadsListItemBinding>>(
        object : DiffUtil.ItemCallback<OfflineVideo>() {
            override fun areItemsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.length == newItem.length
            }
        }) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<FragmentDownloadsListItemBinding> {
        val context = parent.context
        return DataBoundViewHolder<FragmentDownloadsListItemBinding>(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_downloads_list_item, parent, false)).apply {
            binding.root.setOnClickListener { binding.video?.let(clickCallback::startOfflineVideo) }
            binding.root.setOnLongClickListener {
                val delete = context.getString(R.string.delete)
                AlertDialog.Builder(context)
                        .setTitle(delete)
                        .setMessage(context.getString(R.string.are_you_sure))
                        .setPositiveButton(delete) { _, _ -> binding.video?.let(deleteCallback::invoke) }
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show()
                true
            }
        }
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<FragmentDownloadsListItemBinding>, position: Int) {
        holder.binding.video = getItem(position)
    }
}