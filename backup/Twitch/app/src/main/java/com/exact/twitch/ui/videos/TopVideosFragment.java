package com.exact.twitch.ui.videos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.exact.twitch.R;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.util.FragmentUtils;

public class TopVideosFragment extends BaseVideosFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private int[] sortOptions = new int[] { R.string.this_week, R.string.this_month, R.string.all_time };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            int defaultSortItem = sortOptions[0];
            if (getViewModel().getCurrentPeriodText().getValue() == null) {
                getViewModel().getCurrentPeriodText().postValue(getString(defaultSortItem));
            }
            if (getViewModel().getSelectedPeriodItem() == null) {
                getViewModel().setSelectedPeriodItem(defaultSortItem);
            }
            if (getViewModel().getSort() == null) {
                getViewModel().setSort("views");
            }
            getViewModel().getCurrentPeriodText().observe(this, currentSortOption::setText);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sortByContainer.setOnClickListener(v -> FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                sortOptions,
                getViewModel().getSelectedPeriodItem())
        );
    }

    @Override
    public void onSelect(int optionId, CharSequence optionText) {
        if (getViewModel().getSelectedPeriodItem() != optionId) {
            String period = null;
            switch (optionId) {
                case R.string.this_week:
                    period = "week";
                    break;
                case R.string.this_month:
                    period = "month";
                    break;
                case R.string.all_time:
                    period = "all";
                    break;
            }
            getViewModel().setSelectedPeriodItem(optionId);
            getViewModel().setPeriod(period);
            getViewModel().getCurrentPeriodText().postValue(optionText);
            loadData(true);
        }
    }

    @Override
    public void loadData(boolean override) {
        getViewModel().loadVideos(null, getViewModel().getPeriod(), null, null, getViewModel().getSort(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}
