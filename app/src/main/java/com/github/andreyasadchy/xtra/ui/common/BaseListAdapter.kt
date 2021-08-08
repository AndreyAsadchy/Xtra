package com.github.andreyasadchy.xtra.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class BaseListAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, DefaultViewHolder>(diffCallback) {

    protected abstract val layoutId: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return DefaultViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        bind(getItem(position), holder.containerView)
    }

    protected abstract fun bind(item: T, view: View)
}