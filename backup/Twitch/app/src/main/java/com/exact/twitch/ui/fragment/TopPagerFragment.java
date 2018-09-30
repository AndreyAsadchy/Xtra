package com.exact.twitch.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.exact.twitch.ui.pagers.MediaPagerFragment;

public class TopPagerFragment extends MediaPagerFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(new TopPagerAdapter(requireActivity(), getChildFragmentManager()));
    }
}
