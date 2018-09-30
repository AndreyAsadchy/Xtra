package com.exact.twitch.ui.streams;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.ui.main.MainActivity;

public class FollowedStreamsFragment extends BaseStreamsFragment {

    private String userToken;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userToken = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null); //TODO change prefs to auth
        TextView textView = view.findViewById(R.id.fragment_streams_tv_login);
        Button button = view.findViewById(R.id.fragment_streams_btn_login);
        if (userToken == null) {
            textView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> ((MainActivity) getContext()).onLoginClicked());
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {
        getViewModel().loadFollowedStreams(userToken, "all");
    } //TODO add enums instead of strings
}
