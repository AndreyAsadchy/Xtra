package com.github.exact7.xtra.ui.videos

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.ui.common.DataBoundPagedListAdapter

abstract class TempBaseAdapter<T, Binding>(diffCallback: DiffUtil.ItemCallback<T>) : DataBoundPagedListAdapter<T, Binding>(diffCallback) where Binding : ViewDataBinding {
    var lastSelectedItem: T? = null
        protected set
}