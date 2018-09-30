package com.exact.twitch.ui.pagers;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.exact.twitch.ui.clips.FollowedClipsFragment;
import com.exact.twitch.ui.streams.FollowedStreamsFragment;
import com.exact.twitch.ui.videos.FollowedVideosFragment;

public class FollowPagerAdapter extends MediaPagerAdapter {

    private final Bundle args;

    public FollowPagerAdapter(Context context, FragmentManager fm, Bundle args) {
        super(context, fm);
        this.args = args;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FollowedStreamsFragment followedStreamsFragment = new FollowedStreamsFragment();
                followedStreamsFragment.setArguments(args);
                return followedStreamsFragment;
            case 1:
                FollowedVideosFragment followedVideosFragment = new FollowedVideosFragment();
                followedVideosFragment.setArguments(args);
                return followedVideosFragment;
            case 2:
                FollowedClipsFragment followedClipsFragment = new FollowedClipsFragment();
                followedClipsFragment.setArguments(args);
                return followedClipsFragment;
            default:
                return null;
        }
    }
}
