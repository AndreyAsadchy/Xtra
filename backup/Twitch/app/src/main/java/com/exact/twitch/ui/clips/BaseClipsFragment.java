package com.exact.twitch.ui.clips;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.clip.Clip;
import com.exact.twitch.ui.fragment.PagedRecyclerViewFragment;
import com.exact.twitch.ui.fragment.Sortable;

public abstract class BaseClipsFragment extends PagedRecyclerViewFragment<Clip, ClipsViewModel, ClipsRecyclerViewAdapter, BaseClipsFragment.OnClipSelectedListener> implements Sortable {

    public interface OnClipSelectedListener {
        void startClip(Clip clip);
    }

    protected final String TAG = getClass().getSimpleName();

    protected RelativeLayout sortByContainer;
    protected TextView currentSortOption;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            setViewModel(ViewModelProviders.of(this, viewModelFactory).get(ClipsViewModel.class));
            setAdapter(new ClipsRecyclerViewAdapter(itemClickListener, getViewModel()::retry));
            initData();
            initViewModelAndAdapter();
            getViewModel().getSortText().observe(this, currentSortOption::setText);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return isFragmentVisible() ? inflater.inflate(R.layout.fragment_clips, container, false) : null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragment_clips_rv);
        swipeRefresh = view.findViewById(R.id.fragment_clips_refresh);
        sortByContainer = view.findViewById(R.id.fragment_clips_rl_sort_by);
        currentSortOption = view.findViewById(R.id.fragment_clips_tv_sort_option);
        progressBar = view.findViewById(R.id.fragment_clips_progress_bar);
        final int columnCount = getResources().getInteger(R.integer.media_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseClipsFragment.OnClipSelectedListener) {
            itemClickListener = (OnClipSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnClipSelectedListener");
        }
    }
}
