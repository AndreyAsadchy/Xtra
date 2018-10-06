package com.exact.xtra.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.exact.xtra.R
import com.exact.xtra.databinding.FragmentDownloadsListItemBinding
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.ui.DataBoundViewHolder

class DownloadsAdapter(private val clickCallback: DownloadsFragment.OnVideoSelectedListener) : ListAdapter<OfflineVideo, DataBoundViewHolder<FragmentDownloadsListItemBinding>>(
        object : DiffUtil.ItemCallback<OfflineVideo>() {
            override fun areItemsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OfflineVideo, newItem: OfflineVideo): Boolean {
                return oldItem.length == newItem.length
            }
        }) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<FragmentDownloadsListItemBinding> {
        return DataBoundViewHolder<FragmentDownloadsListItemBinding>(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.fragment_downloads_list_item, parent, false)).apply { binding.root.setOnClickListener { binding.video?.let(clickCallback::startOfflineVideo) } }
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<FragmentDownloadsListItemBinding>, position: Int) {
        holder.binding.video = getItem(position)
    }
}