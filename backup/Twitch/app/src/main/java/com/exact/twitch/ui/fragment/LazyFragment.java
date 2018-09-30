package com.exact.twitch.ui.fragment;

import androidx.fragment.app.Fragment;

public class LazyFragment extends Fragment {

    private boolean isVisible;
    private boolean loaded;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            if (!loaded) {
                getFragmentManager().beginTransaction().detach(this).attach(this).commit();
                loaded = true;
            }
        } else {
            isVisible = false;
        }
    }

    public boolean isFragmentVisible() {
        return isVisible;
    }
}
