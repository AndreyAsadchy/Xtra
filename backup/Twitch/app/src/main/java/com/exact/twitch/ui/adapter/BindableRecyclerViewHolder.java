package com.exact.twitch.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

abstract class BindableRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    BindableRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    protected abstract void bindTo(T item);
}
