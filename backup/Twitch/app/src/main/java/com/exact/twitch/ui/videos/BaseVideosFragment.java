package com.exact.twitch.ui.videos;

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
import com.exact.twitch.model.video.Video;
import com.exact.twitch.ui.fragment.PagedRecyclerViewFragment;
import com.exact.twitch.ui.fragment.Sortable;

public abstract class BaseVideosFragment extends PagedRecyclerViewFragment<Video, VideosViewModel, VideosRecyclerViewAdapter, BaseVideosFragment.OnVideoSelectedListener> implements Sortable {

    public interface OnVideoSelectedListener {
        void startVideo(Video video);
    }

    protected final String TAG = getClass().getSimpleName();

    protected RelativeLayout sortByContainer;
    protected TextView currentSortOption;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            setViewModel(ViewModelProviders.of(this, viewModelFactory).get(VideosViewModel.class));
            setAdapter(new VideosRecyclerViewAdapter(itemClickListener, getViewModel()::retry));
            initData();
            initViewModelAndAdapter();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return isFragmentVisible() ? inflater.inflate(R.layout.fragment_videos, container, false) : null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragment_videos_rv);
        swipeRefresh = view.findViewById(R.id.fragment_videos_refresh);
        sortByContainer = view.findViewById(R.id.fragment_videos_rl_sort_by);
        currentSortOption = view.findViewById(R.id.fragment_videos_tv_sort_option);
        progressBar = view.findViewById(R.id.fragment_videos_progress_bar);
        final int columnCount = getResources().getInteger(R.integer.media_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseVideosFragment.OnVideoSelectedListener) {
            itemClickListener = (OnVideoSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnVideoSelectedListener");
        }
    }
}
