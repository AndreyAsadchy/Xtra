package com.exact.twitch.ui.videos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.exact.twitch.R;

public class GameVideosFragment extends BaseVideosFragment implements VideosSortDialog.OnSortOptionSelected {

    private String game;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            if (getViewModel().getCurrentOrderText().getValue() == null) {
                getViewModel().getCurrentOrderText().postValue(getString(R.string.view_count));
            }
            if (getViewModel().getSelectedOrderItem() == null) {
                getViewModel().setSelectedOrderItem(R.id.fragment_video_sort_dialog_views);
            }
            if (getViewModel().getCurrentPeriodText().getValue() == null) {
                getViewModel().getCurrentPeriodText().postValue(getString(R.string.this_week));
            }
            if (getViewModel().getSelectedPeriodItem() == null) {
                getViewModel().setSelectedPeriodItem(R.id.fragment_video_sort_dialog_week);
            }
            if (getViewModel().getSort() == null) {
                getViewModel().setSort("views");
            }
            getViewModel().getCurrentOrderText().observe(this, text -> currentSortOption.setText(getString(R.string.order_and_period, text, getViewModel().getCurrentPeriodText().getValue())));
            getViewModel().getCurrentPeriodText().observe(this, text -> currentSortOption.setText(getString(R.string.order_and_period, getViewModel().getCurrentOrderText().getValue(), text)));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            game = getArguments().getString("game");
        }
        sortByContainer.setOnClickListener(v -> VideosSortDialog.newInstance(getViewModel().getSelectedOrderItem(), getViewModel().getSelectedPeriodItem())                .show(getChildFragmentManager(), null));
    }

    @Override
    public void onSort(int orderItemId, CharSequence orderText, int periodItemId, CharSequence periodText) {
        boolean shouldReload = false;
        if (getViewModel().getSelectedOrderItem() != orderItemId) {
            shouldReload = true;
            getViewModel().setSort(orderItemId == R.id.fragment_video_sort_dialog_time ? "time" : "views");
            getViewModel().getCurrentOrderText().postValue(orderText);
            getViewModel().setSelectedOrderItem(orderItemId);
        }
        if (getViewModel().getSelectedPeriodItem() != periodItemId) {
            shouldReload = true;
            String period = null;
            switch (periodItemId) {
                case R.id.fragment_video_sort_dialog_week:
                    period = "week";
                    break;
                case R.id.fragment_video_sort_dialog_month:
                    period = "month";
                    break;
                case R.id.fragment_video_sort_dialog_all:
                    period = "all";
                    break;
            }
            getViewModel().setSelectedPeriodItem(periodItemId);
            getViewModel().setPeriod(period);
            getViewModel().getCurrentPeriodText().postValue(periodText);
        }
        if (shouldReload) {
            loadData(true);
        }
    }

    @Override
    public void loadData(boolean override) {
        getViewModel().loadVideos(game, getViewModel().getPeriod(), null, null, getViewModel().getSort(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}
