package com.github.exact7.xtra.ui

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

abstract class DataBoundPagedListAdapter<T, V : ViewDataBinding>(
        diffCallback: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, DataBoundViewHolder<V>>(diffCallback) {

    protected abstract fun createBinding(parent: ViewGroup): V
    protected abstract fun bind(binding: V, item: T?)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DataBoundViewHolder(createBinding(parent))

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position))
    }

    //TODO instead of this change viewmodel to load data inside it and not from fragment, and add livedata query field
    override fun submitList(pagedList: PagedList<T>?) {
        pagedList?.let {
            if (it.size > 0) {
                super.submitList(it)
            } else {
                it.addWeakCallback(pagedList.snapshot(), object : PagedList.Callback() {
                    override fun onChanged(position: Int, count: Int) {
                    }

                    override fun onInserted(position: Int, count: Int) {
                        super@DataBoundPagedListAdapter.submitList(it)

                    }

                    override fun onRemoved(position: Int, count: Int) {

                    }
                })
            }
            return@submitList
        }
        super.submitList(pagedList)
    }
}