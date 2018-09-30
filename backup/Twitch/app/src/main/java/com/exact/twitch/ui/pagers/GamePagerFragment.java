package com.exact.twitch.ui.pagers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

public class GamePagerFragment extends MediaPagerFragment {

    public static GamePagerFragment newInstance(String game) {
        Bundle args = new Bundle();
        args.putString("game", game);
        GamePagerFragment fragment = new GamePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(new GamePagerAdapter(requireActivity(), getChildFragmentManager(), getArguments()));
    }
}
