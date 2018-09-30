package com.exact.twitch.ui.fragment;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;

import com.exact.twitch.di.Injectable;

import javax.inject.Inject;

public abstract class RecyclerViewFragment<VM extends ViewModel, Adapter extends RecyclerView.Adapter, Listener> extends LazyFragment implements Injectable {

    protected RecyclerView recyclerView;
    private VM viewModel;
    private Adapter adapter;
    protected Listener itemClickListener;
    protected final String TAG = getClass().getSimpleName();

    @Inject
    protected ViewModelProvider.Factory viewModelFactory;

    protected VM getViewModel() {
        return viewModel;
    }

    protected void setViewModel(VM viewModel) {
        this.viewModel = viewModel;
    }

    protected Adapter getAdapter() {
        return adapter;
    }

    protected void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        itemClickListener = null;
    }

    public void smoothScrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }
}