package com.github.exact7.xtra.ui

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class DataBoundViewHolder<out T : ViewDataBinding> (val binding: T) : RecyclerView.ViewHolder(binding.root)