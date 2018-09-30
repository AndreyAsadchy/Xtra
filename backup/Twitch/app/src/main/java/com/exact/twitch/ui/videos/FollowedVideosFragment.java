package com.exact.twitch.ui.videos;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.ui.main.MainActivity;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.util.FragmentUtils;

public class FollowedVideosFragment extends BaseVideosFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private int[] sortOptions = new int[] { R.string.upload_date, R.string.view_count };
    private String userToken;

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
        userToken = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null);
        sortByContainer.setOnClickListener(v -> FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                sortOptions,
                getViewModel().getSelectedOrderItem())
        );
        TextView textView = view.findViewById(R.id.fragment_videos_tv_login);
        Button button = view.findViewById(R.id.fragment_videos_btn_login);
        if (userToken == null) {
            textView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> ((MainActivity) getContext()).onLoginClicked());
            progressBar.setVisibility(View.GONE);
            sortByContainer.setVisibility(View.GONE);
        }
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
        getViewModel().loadFollowedVideos(userToken, null, null, getViewModel().getSort(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}
