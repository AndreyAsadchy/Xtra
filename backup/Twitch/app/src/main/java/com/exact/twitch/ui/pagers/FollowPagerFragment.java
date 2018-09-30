package com.exact.twitch.ui.pagers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

public class FollowPagerFragment extends MediaPagerFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = new Bundle(1);
        args.putString("token", "ebn5a0wxof6x770yx5r2udc0876t44"); //TODO use shared preferences
        setAdapter(new FollowPagerAdapter(requireActivity(), getChildFragmentManager(), args));
    }
}
