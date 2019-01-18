package com.github.exact7.xtra.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

abstract class DataBoundPagedListAdapter<T, V : ViewDataBinding>(
        diffCallback: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, DataBoundViewHolder<V>>(diffCallback) {

    protected abstract val itemId: Int
    protected abstract fun bind(binding: V, item: T?)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DataBoundViewHolder(
            DataBindingUtil.inflate<V>(LayoutInflater.from(parent.context), itemId, parent, false)
    )

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position))
    }
}