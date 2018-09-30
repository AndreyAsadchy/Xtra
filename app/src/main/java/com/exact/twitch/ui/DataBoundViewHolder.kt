package com.exact.twitch.ui

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class DataBoundViewHolder<out T : ViewDataBinding> (val binding: T) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)