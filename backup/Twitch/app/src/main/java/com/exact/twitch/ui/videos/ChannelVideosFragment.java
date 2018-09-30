package com.exact.twitch.ui.videos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.exact.twitch.R;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.util.FragmentUtils;

public class ChannelVideosFragment extends BaseVideosFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private int[] sortOptions = new int[] { R.string.upload_date, R.string.view_count };

    private Object channelId;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            int defaultSortItem = sortOptions[0];
            if (getViewModel().getSelectedOrderItem() == null) {
                getViewModel().setSelectedOrderItem(defaultSortItem);
            }
            if (getViewModel().getCurrentOrderText().getValue() == null) {
                getViewModel().getCurrentOrderText().setValue(getString(defaultSortItem));
            }
            if (getViewModel().getSort() == null) {
                getViewModel().setSort("time");
            }
            getViewModel().getCurrentOrderText().observe(this, currentSortOption::setText);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            channelId = getArguments().get("channelId");
        }
        sortByContainer.setOnClickListener(v -> FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                sortOptions,
                getViewModel().getSelectedOrderItem())
        );
    }

    @Override
    public void onSelect(int optionId, CharSequence optionText) {
        if (getViewModel().getSelectedOrderItem() != optionId) {
            getViewModel().setSort(optionId == R.string.upload_date ? "time" : "views");
            getViewModel().setSelectedOrderItem(optionId);
            getViewModel().getCurrentOrderText().postValue(optionText);
            loadData(true);
        }
    }

    @Override
    public void loadData(boolean override) {
        getViewModel().loadChannelVideos(channelId, null, getViewModel().getSort(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}
