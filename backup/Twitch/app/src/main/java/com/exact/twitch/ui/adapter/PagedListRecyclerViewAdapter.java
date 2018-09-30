package com.exact.twitch.ui.adapter;

import androidx.paging.PagedListAdapter;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;
import com.exact.twitch.repository.LoadingState;

import java.util.List;

public abstract class PagedListRecyclerViewAdapter<T, VH extends BindableRecyclerViewHolder<T>, ItemClickListener> extends PagedListAdapter<T, RecyclerView.ViewHolder> {

    private final int itemResId;
    private final Class<VH> viewHolderClass;
    protected final ItemClickListener itemClickListener;
    private final Runnable retryCallback;
    private LoadingState networkLoadingState;

    PagedListRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, int itemResId, Class<VH> viewHolderClass, ItemClickListener itemClickListener, Runnable retryCallback) {
        super(diffCallback);
        this.itemResId = itemResId;
        this.viewHolderClass = viewHolderClass;
        this.itemClickListener = itemClickListener;
        this.retryCallback = retryCallback;
    }

    public void setLoadingState(LoadingState networkLoadingState) {
        LoadingState previousLoadingState = this.networkLoadingState;
        boolean hadExtraRow = hasExtraRow();
        this.networkLoadingState = networkLoadingState;
        boolean hasExtraRow = hasExtraRow();
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (hasExtraRow && previousLoadingState != networkLoadingState) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == itemResId) {
            try {
                System.out.println(viewHolderClass.getConstructors().length);
                return viewHolderClass.getConstructor(getClass(), View.class).newInstance(this, LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)); //TODO 2 argument constructor probably because of ProGuard
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NetworkStateItemViewHolder.create(parent, retryCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == itemResId) {
            viewHolderClass.cast(holder).bindTo(getItem(position));
        } else {
            ((NetworkStateItemViewHolder) holder).bindTo(networkLoadingState);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            onItemUpdate(viewHolderClass.cast(holder), position, payloads);
        }
    }

    protected abstract void onItemUpdate(VH holder, int position, @NonNull List<Object> payloads);

    @Override
    public int getItemCount() {
        return super.getItemCount() + (hasExtraRow() ? 1 : 0);
    }

    private boolean hasExtraRow() {
        return networkLoadingState != null && networkLoadingState != LoadingState.LOADED;
    }

    @Override
    public int getItemViewType(int position) {
        return hasExtraRow() && position == getItemCount() - 1 ? R.layout.network_state_item : itemResId;
    }
}
