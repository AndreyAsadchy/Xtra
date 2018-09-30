package com.exact.twitch.ui.clips;

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

public class FollowedClipsFragment extends BaseClipsFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private int[] sortOptions = new int[] { R.string.trending, R.string.view_count };
    private String userToken;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFragmentVisible()) {
            int defaultSortItem = sortOptions[1];
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
        userToken = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null);
        sortByContainer.setOnClickListener(v -> FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                sortOptions,
                getViewModel().getSelectedSortItem())
        );
        TextView textView = view.findViewById(R.id.fragment_clips_tv_login);
        Button button = view.findViewById(R.id.fragment_clips_btn_login);
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
        if (getViewModel().getSelectedSortItem() != optionId) {
            getViewModel().setTrending(optionId == R.string.trending);
            getViewModel().setSelectedSortItem(optionId);
            getViewModel().getSortText().postValue(optionText);
            loadData(true);
        }
    }

    @Override
    public void loadData(boolean override) {
        getViewModel().loadFollowedClips(userToken, getViewModel().getTrending(), override);
    }

    @Override
    protected void initData() {
        loadData(false);
    }
}
