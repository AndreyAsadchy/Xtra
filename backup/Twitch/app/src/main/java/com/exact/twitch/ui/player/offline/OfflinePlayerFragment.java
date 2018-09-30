package com.exact.twitch.ui.player.offline;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exact.twitch.R;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.ui.player.BasePlayerFragment;

public class OfflinePlayerFragment extends BasePlayerFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private OfflinePlayerViewModel viewModel;
//    private ChatRecyclerView chatRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View viewById = view.findViewById(R.id.player_settings);
        if (viewById != null) {
            viewById.setVisibility(View.GONE);
            View viewById1 = view.findViewById(R.id.player_download);
            if (viewById1 != null) {
                viewById1.setVisibility(View.GONE);

            }

        }
//        chatRecyclerView = view.findViewById(R.id.fragment_player_chat_rv);
//        channelBtn.setOnClickListener(v -> channelListener.viewChannel(video.getChannel().getName()));
        //TODO morebtn
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(OfflinePlayerViewModel.class);
        pvPlayer.setPlayer(viewModel.getPlayer());
        if (viewModel.isFirstLaunch()) {
            viewModel.setVideo(getArguments().getParcelable("video"));
            viewModel.play();
        }
//        viewModel.getChatMessages().observe(this, chatRecyclerView::submitList);
//        viewModel.getNewMessage().observe(this, message -> chatRecyclerView.notifyAdapter());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!requireActivity().isChangingConfigurations()) {
            viewModel.getPlayer().release();
        }
    }

    @Override
    public void onSelect(int optionId, CharSequence optionText) {

    }
}
