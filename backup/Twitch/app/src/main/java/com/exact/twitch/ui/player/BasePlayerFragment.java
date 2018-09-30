package com.exact.twitch.ui.player;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ImageButton;

import com.exact.twitch.R;
import com.exact.twitch.di.Injectable;
import com.exact.twitch.ui.fragment.OnChannelClickedListener;
import com.exact.twitch.ui.view.SlidingView;
import com.exact.twitch.ui.view.draggableview.DraggableListener;
import com.exact.twitch.ui.view.draggableview.DraggableView;
import com.google.android.exoplayer2.ui.PlayerView;

import javax.inject.Inject;

public abstract class BasePlayerFragment extends Fragment implements Injectable {

    protected final String TAG = getClass().getSimpleName();
    protected PlayerView pvPlayer;
    protected DraggableView draggableView;
    protected SlidingView slidingView;
    protected ImageButton channelBtn;
    protected ImageButton moreBtn;
    protected OnChannelClickedListener channelListener;

    private DraggableListener dragListener;
    private boolean isPortraitOrientation;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pvPlayer = view.findViewById(R.id.fragment_player_pv_player);
        channelBtn = view.findViewById(R.id.player_profile);
        moreBtn = view.findViewById(R.id.player_more);
        ImageButton minimizeBtn = view.findViewById(R.id.player_minimize);
        minimizeBtn.setOnClickListener(v -> minimize());
        ImageButton fullscreenBtn;
        if (isPortraitOrientation) {
            draggableView = view.findViewById(R.id.fragment_player_draggable_view);
            draggableView.setDraggableListener(dragListener);
            fullscreenBtn = view.findViewById(R.id.player_fullscreen);
            fullscreenBtn.setOnClickListener(v -> requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        } else {
//            slidingView = view.findViewById(R.id.fragment_player_sv);
            fullscreenBtn = view.findViewById(R.id.player_fullscreen_exit);
            fullscreenBtn.setOnClickListener(v -> requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!requireActivity().isChangingConfigurations()) {
            pvPlayer.setPlayer(null);
        }
    }

    public void minimize() {
        if (isPortraitOrientation) {
            draggableView.minimize();
        } else {
//            slidingView.minimize();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChannelClickedListener) {
            channelListener = (OnChannelClickedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnViewChannelClickedListener");
        }
        if (context instanceof DraggableListener) {
            dragListener = (DraggableListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DraggableListener");
        }
    }
}
