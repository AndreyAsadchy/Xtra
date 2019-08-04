package com.github.exact7.xtra.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.github.exact7.xtra.R
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.paging_item.*

abstract class BasePagedListAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, DefaultViewHolder>(diffCallback) {

    protected abstract val layoutId: Int
    private var pagingState: LoadingState? = null

    protected abstract fun bind(item: T, view: View)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        return DefaultViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        if (getItemViewType(position) == layoutId) {
            bind(getItem(position)!!, holder.containerView)
        } else {
            holder.progressBar.visible(pagingState == LoadingState.LOADING)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!hasExtraRow() || position != itemCount - 1) {
            layoutId
        } else {
            R.layout.paging_item
        }
    }

    fun setPagingState(pagingState: LoadingState) {
        val previousState = pagingState
        val hadExtraRow = hasExtraRow()
        this.pagingState = pagingState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            notifyItemChanged(super.getItemCount())
        } else if (hasExtraRow && previousState != pagingState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    private fun hasExtraRow() = pagingState != null && pagingState != LoadingState.LOADED
}