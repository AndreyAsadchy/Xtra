package com.exact.twitch.ui.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public abstract class ItemAwareFragmentPagerAdapter extends FragmentPagerAdapter {

    private Fragment currentFragment;

    ItemAwareFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentFragment = (Fragment) object;
    }
}
