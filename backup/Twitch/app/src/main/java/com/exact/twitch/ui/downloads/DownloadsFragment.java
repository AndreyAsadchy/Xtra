package com.exact.twitch.ui.downloads;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;
import com.exact.twitch.di.Injectable;
import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.ui.fragment.PagedRecyclerViewFragment;

public class DownloadsFragment extends PagedRecyclerViewFragment<OfflineVideo, DownloadsViewModel, DownloadsRecyclerViewAdapter, DownloadsFragment.OnVideoSelectedListener> implements Injectable {

    @Override
    protected void initData() {

    }

    public interface OnVideoSelectedListener {
        void startOfflineVideo(OfflineVideo video);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_downloads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragment_downloads_rv);
        progressBar = view.findViewById(R.id.fragment_downloads_progress_bar);
        swipeRefresh = view.findViewById(R.id.fragment_downloads_refresh);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DownloadsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(DownloadsViewModel.class);
        setViewModel(viewModel);
        setAdapter(new DownloadsRecyclerViewAdapter(itemClickListener, viewModel::retry));
        final int columnCount = getResources().getInteger(R.integer.media_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
        initViewModelAndAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoSelectedListener) {
            itemClickListener = (OnVideoSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnVideoSelectedListener");
        }
    }
}
