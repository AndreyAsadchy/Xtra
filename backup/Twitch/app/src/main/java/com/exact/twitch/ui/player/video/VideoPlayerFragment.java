package com.exact.twitch.ui.player.video;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.exact.twitch.R;
import com.exact.twitch.ui.VideoDownloadDialog;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.ui.player.BasePlayerFragment;
import com.exact.twitch.ui.player.PlayerViewModel;
import com.exact.twitch.util.FragmentUtils;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;

import java.util.LinkedList;
import java.util.List;

public class VideoPlayerFragment extends BasePlayerFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener, VideoDownloadDialog.OnDownloadClickListener {

    private VideoPlayerViewModel viewModel;
//    private ChatRecyclerView chatRecyclerView;
private ImageButton downloadBtn;
    private ImageButton settingsBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        chatRecyclerView = view.findViewById(R.id.fragment_player_chat_rv);
        downloadBtn = view.findViewById(R.id.player_download);
        settingsBtn = view.findViewById(R.id.player_settings);
//        channelBtn.setOnClickListener(v -> channelListener.viewChannel(video.getChannel().getName()));
        settingsBtn.setOnClickListener(v -> {
            LinkedList<CharSequence> list = new LinkedList<>(viewModel.getQualities().getValue());
            list.addFirst(getString(R.string.auto));
            FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                    requireActivity(),
                    getChildFragmentManager(),
                    list,
                    viewModel.getSelectedQualityIndex());
        });
        if (downloadBtn != null)
        downloadBtn.setOnClickListener(v -> new VideoDownloadDialog(this, viewModel.getQualities().getValue(), viewModel.getSegments()).show());
        //TODO morebtn
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VideoPlayerViewModel.class);
        pvPlayer.setPlayer(viewModel.getPlayer());
        if (viewModel.isFirstLaunch()) {
            settingsBtn.setEnabled(false);
            if (downloadBtn != null)
            downloadBtn.setEnabled(false);
            viewModel.setVideo(getArguments().getParcelable("video"));
            viewModel.getQualities().observe(this, list -> {
                boolean enabled = list != null;
                if (downloadBtn != null)
                downloadBtn.setEnabled(enabled);
                settingsBtn.setEnabled(enabled);
            });
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
        if (viewModel.getSelectedQualityIndex() != optionId) {
            if (optionId == 0) {
                viewModel.enableAutoMode();
            } else if (optionId < viewModel.getQualities().getValue().size()) {
                viewModel.changeQuality(optionId - 1);
                viewModel.setSelectedQualityIndex(optionId);
            } else {
                viewModel.setPlayerMode(PlayerViewModel.PlayerMode.AUDIO_ONLY);
            }
        }
    }

    @Override
    public void onClick(String quality, List<RenditionKey> keys) {
        viewModel.download(quality, keys);
    }
}
