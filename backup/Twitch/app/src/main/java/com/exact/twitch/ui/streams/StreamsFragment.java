package com.exact.twitch.ui.streams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

public class StreamsFragment extends BaseStreamsFragment {

    private String game;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) { //init in onViewCreated, because if view is not visible it's not needed
            game = getArguments().getString("game");
        }
    }

    @Override
    protected void initData() {
        getViewModel().loadStreams(game, null, "live");
    }
}
