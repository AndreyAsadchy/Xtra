package com.exact.twitch.ui.pagers;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.exact.twitch.ui.clips.ClipsFragment;
import com.exact.twitch.ui.streams.StreamsFragment;
import com.exact.twitch.ui.videos.GameVideosFragment;

public class GamePagerAdapter extends MediaPagerAdapter {

    private final Bundle args;

    public GamePagerAdapter(Context context, FragmentManager fm, Bundle args) {
        super(context, fm);
        this.args = args;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                StreamsFragment streamsFragment = new StreamsFragment();
                streamsFragment.setArguments(args);
                return streamsFragment;
            case 1:
                GameVideosFragment gameVideosFragment = new GameVideosFragment();
                gameVideosFragment.setArguments(args);
                return gameVideosFragment;
            case 2:
                ClipsFragment clipsFragment = new ClipsFragment();
                clipsFragment.setArguments(args);
                return clipsFragment;
            default:
                return null;
        }
    }
}
