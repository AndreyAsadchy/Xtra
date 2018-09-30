package com.exact.twitch.ui.clips;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.exact.twitch.R;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.util.FragmentUtils;

public class ClipsFragment extends BaseClipsFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private String channelName;
    private String game;

    private int[] sortOptions = new int[] { R.string.trending, R.string.today, R.string.this_week, R.string.this_month, R.string.all_time };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            int defaultSortItem = sortOptions[2];
            if (getViewModel().getSelectedSortItem() == null) {
                getViewModel().setSelectedSortItem(defaultSortItem);
            }
            if (getViewModel().getSortText().getValue() == null) {
                getViewModel().getSortText().setValue(getString(defaultSortItem));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            channelName = getArguments().getString("channel");
            game = getArguments().getString("game");
        }
        sortByContainer.setOnClickListener(v -> FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                sortOptions,
                getViewModel().getSelectedSortItem())
        );
    }

    @Override
    public void onSelect(int optionId, CharSequence optionText) {
        if (getViewModel().getSelectedSortItem() != optionId) {
            String period = null;
            boolean trending = false;
            switch (optionId) {
                case R.string.trending:
                    trending = true;
                    break;
                case R.string.today:
                    period = "day";
                    break;
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
            getViewModel().setSelectedSortItem(optionId);
            getViewModel().setPeriod(period);
            getViewModel().setTrending(trending);
            getViewModel().getSortText().postValue(optionText);
            loadData(true);
        }
    }

    @Override
    public void loadData(boolean override) {
        getViewModel().loadClips(channelName, game, null, getViewModel().getPeriod(), getViewModel().getTrending(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}