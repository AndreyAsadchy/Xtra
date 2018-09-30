package com.exact.twitch.ui.player.clip;

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
import com.exact.twitch.util.FragmentUtils;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;

import java.util.List;

public class ClipPlayerFragment extends BasePlayerFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener, VideoDownloadDialog.OnDownloadClickListener {

    private ClipPlayerViewModel viewModel;
//    private ChatRecyclerView chatRecyclerView;
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
        settingsBtn = view.findViewById(R.id.player_settings);
//        channelBtn.setOnClickListener(v -> channelListener.viewChannel(clip.getBroadcaster().getName()));
        settingsBtn.setOnClickListener(v ->
                FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                requireActivity(),
                getChildFragmentManager(),
                viewModel.getQualities().getValue(),
                viewModel.getSelectedQualityIndex()));
        ImageButton downloadBtn = view.findViewById(R.id.player_download);
        downloadBtn.setOnClickListener(v -> new VideoDownloadDialog(this, viewModel.getQualities().getValue(), null).show());
        //TODO morebtn
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ClipPlayerViewModel.class);
        pvPlayer.setPlayer(viewModel.getPlayer());
        if (viewModel.isFirstLaunch()) {
            viewModel.setClip(getArguments().getParcelable("clip"));
            settingsBtn.setEnabled(true);
            viewModel.getQualities().observe(this, list -> settingsBtn.setEnabled(list != null));
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
            viewModel.changeQuality(optionId);
            viewModel.setSelectedQualityIndex(optionId);

        }
    }

    @Override
    public void onClick(String quality, List<RenditionKey> keys) {
        viewModel.download(quality);
    }
}
