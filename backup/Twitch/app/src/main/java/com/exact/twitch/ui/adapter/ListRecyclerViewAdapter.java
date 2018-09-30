package com.exact.twitch.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ListRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder, ItemClickListener> extends ListAdapter<T, VH> {

    final ItemClickListener listener;

    protected ListRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, ItemClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }
}
