package com.github.exact7.xtra.ui

import androidx.databinding.ViewDataBinding

class DataBoundViewHolder<out T : ViewDataBinding> (val binding: T) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)