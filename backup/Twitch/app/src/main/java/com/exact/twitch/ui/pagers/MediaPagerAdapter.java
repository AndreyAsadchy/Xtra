package com.exact.twitch.ui.pagers;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.exact.twitch.R;

public abstract class MediaPagerAdapter extends ItemAwareFragmentPagerAdapter {

    private final Context context;

    public MediaPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        int res = 0;
        switch (position) {
            case 0:
                res = R.string.live;
                break;
            case 1:
                res = R.string.videos;
                break;
            case 2:
                res = R.string.clips;
                break;
        }
        return context.getString(res);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
