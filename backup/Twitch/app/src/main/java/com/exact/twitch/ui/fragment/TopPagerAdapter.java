package com.exact.twitch.ui.fragment;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.exact.twitch.ui.clips.ClipsFragment;
import com.exact.twitch.ui.pagers.MediaPagerAdapter;
import com.exact.twitch.ui.streams.StreamsFragment;
import com.exact.twitch.ui.videos.TopVideosFragment;

public class TopPagerAdapter extends MediaPagerAdapter {

    public TopPagerAdapter(Context context, FragmentManager fm) {
        super(context, fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new StreamsFragment();
            case 1:
                return new TopVideosFragment();
            case 2:
                return new ClipsFragment();
            default:
                return null;
        }
    }
}
