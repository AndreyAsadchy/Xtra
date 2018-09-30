package com.exact.twitch.ui.streams;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;
import com.exact.twitch.model.stream.Stream;
import com.exact.twitch.ui.fragment.PagedRecyclerViewFragment;

public abstract class BaseStreamsFragment extends PagedRecyclerViewFragment<Stream, StreamsViewModel, StreamsRecyclerViewAdapter, BaseStreamsFragment.OnStreamSelectedListener> {

    public interface OnStreamSelectedListener {
        void startStream(Stream stream);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return isFragmentVisible() ? inflater.inflate(R.layout.fragment_streams, container, false) : null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragment_streams_rv);
        swipeRefresh = view.findViewById(R.id.fragment_streams_refresh);
        progressBar = view.findViewById(R.id.fragment_streams_progress_bar);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        final int columnCount = getResources().getInteger(R.integer.media_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            setViewModel(ViewModelProviders.of(this, viewModelFactory).get(StreamsViewModel.class));
            setAdapter(new StreamsRecyclerViewAdapter(itemClickListener, getViewModel()::retry));
            initData();
            initViewModelAndAdapter();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStreamSelectedListener) {
            itemClickListener = (OnStreamSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStreamSelectedListener");
        }
    }
}
