package com.exact.twitch.ui.pagers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;

public abstract class MediaPagerFragment extends Fragment implements ItemAwarePagerFragment {

    private ItemAwareFragmentPagerAdapter adapter;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.fragment_media_pager_vp);
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public Fragment getCurrentFragment() {
        return adapter.getCurrentFragment();
    }

    protected void setAdapter(ItemAwareFragmentPagerAdapter adapter) {
        this.adapter = adapter;
        viewPager.setAdapter(adapter);
    }
}
