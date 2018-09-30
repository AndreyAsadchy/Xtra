package com.exact.twitch.ui.games;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;
import com.exact.twitch.model.game.Game;
import com.exact.twitch.ui.fragment.PagedRecyclerViewFragment;

public class GamesFragment extends PagedRecyclerViewFragment<Game, GamesViewModel, GamesRecyclerViewAdapter, GamesFragment.OnGameSelectedListener> {

    public interface OnGameSelectedListener {
        void findStreamsByGame(Game game);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragment_games_rv_top_games);
        swipeRefresh = view.findViewById(R.id.fragment_games_refresh);
        progressBar = view.findViewById(R.id.fragment_games_progress_bar);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViewModel(ViewModelProviders.of(this, viewModelFactory).get(GamesViewModel.class));
        setAdapter(new GamesRecyclerViewAdapter(itemClickListener, getViewModel()::retry));
        initViewModelAndAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameSelectedListener) {
            itemClickListener = (OnGameSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnGameSelectedListener");
        }
    }

    @Override
    protected void initData() {
        //loads automatically in viewmodel constructor
    }
}
