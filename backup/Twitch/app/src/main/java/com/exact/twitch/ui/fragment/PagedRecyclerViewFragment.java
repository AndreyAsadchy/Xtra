package com.exact.twitch.ui.fragment;

import android.os.Parcelable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;

import com.exact.twitch.repository.LoadingState;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.exact.twitch.ui.viewmodel.PagedListViewModel;

public abstract class PagedRecyclerViewFragment<T extends Parcelable, VM extends PagedListViewModel<T>, Adapter extends PagedListRecyclerViewAdapter<T, ?, Listener>, Listener> extends RecyclerViewFragment<VM, Adapter, Listener> {

    protected SwipeRefreshLayout swipeRefresh;
    protected ProgressBar progressBar;

    @Override
    protected void setViewModel(VM viewModel) {
        super.setViewModel(viewModel);
        swipeRefresh.setOnRefreshListener(viewModel::refresh);
    }

    protected abstract void initData();

    protected void initViewModelAndAdapter() {
        getViewModel().getRefreshState().observe(this, networkState -> {
            swipeRefresh.setRefreshing(!isFirstLoad() && networkState == LoadingState.LOADING);
            progressBar.setVisibility(isFirstLoad() && networkState == LoadingState.LOADING ? View.VISIBLE : View.GONE);
        });
        getViewModel().getList().observe(this, getAdapter()::submitList);
        getViewModel().getLoadingState().observe(this, getAdapter()::setLoadingState);
    }

    private boolean isFirstLoad() {
        return getViewModel().getList().getValue() == null;
    }
}
