package com.exact.twitch.ui.player.stream;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.exact.twitch.R;
import com.exact.twitch.tasks.LiveChatTask;
import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment;
import com.exact.twitch.ui.player.BasePlayerFragment;
import com.exact.twitch.ui.player.PlayerViewModel;
import com.exact.twitch.ui.view.ChatRecyclerView;
import com.exact.twitch.ui.view.MessageView;
import com.exact.twitch.util.FragmentUtils;

import java.util.LinkedList;

public class StreamPlayerFragment extends BasePlayerFragment implements RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener {

    private StreamPlayerViewModel viewModel;
    private LiveChatTask chatThread;
    private ChatRecyclerView chatRecyclerView;
    private MessageView messageView;
    private ImageButton settingsBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_stream, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatRecyclerView = view.findViewById(R.id.fragment_player_chat_rv);
        messageView = view.findViewById(R.id.fragment_player_cmv_chat_message);
        settingsBtn = view.findViewById(R.id.player_settings);
//        channelBtn.setOnClickListener(v -> channelListener.viewChannel(stream.getChannel().getName()));

        //TODO morebtn
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamPlayerViewModel.class);
        messageView.setVisibility(View.VISIBLE);
        pvPlayer.setPlayer(viewModel.getPlayer());
        if (viewModel.isFirstLaunch()) {
            viewModel.setStream(getArguments().getParcelable("stream"));
            settingsBtn.setEnabled(false);
            SharedPreferences prefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
            String userName = prefs.getString("username", null);
            String userToken = prefs.getString("token", null);
            if (userToken != null) {

            }
            viewModel.play(userName, userToken, thread -> {
                if (userToken != null) { //authorized
                    messageView.setCallback(thread);
                }
                chatThread = thread;
            });
            viewModel.getQualities().observe(this, list -> settingsBtn.setEnabled(list != null));
        }
        viewModel.getChatMessages().observe(this, chatRecyclerView::submitList);
        viewModel.getNewMessage().observe(this, message -> chatRecyclerView.notifyAdapter());
        settingsBtn.setOnClickListener(v -> {
            LinkedList<CharSequence> list = new LinkedList<>(viewModel.getQualities().getValue());
            list.addFirst(getString(R.string.auto));
            list.addLast(getString(R.string.chat_only));
            FragmentUtils.INSTANCE.showRadioButtonDialogFragment(
                    requireActivity(),
                    getChildFragmentManager(),
                    list,
                    viewModel.getSelectedQualityIndex());
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!requireActivity().isChangingConfigurations()) {
            viewModel.getPlayer().release();
            //TODO
//            chatThread.shutdown();
        }
    }

    @Override
    public void onSelect(int optionId, CharSequence optionText) {
        if (viewModel.getSelectedQualityIndex() != optionId) {
            if (optionId == 0) {
                viewModel.enableAutoMode();
            } else {
                int qualitiesCount = viewModel.getQualities().getValue().size();
                if (optionId < qualitiesCount) {
                    viewModel.changeQuality(optionId - 1);
                } else if (optionId == qualitiesCount) { //last item is audio only
                    viewModel.setPlayerMode(PlayerViewModel.PlayerMode.AUDIO_ONLY);
                } else { //chat only
                    viewModel.setPlayerMode(PlayerViewModel.PlayerMode.DISABLED);
                }
            }
            viewModel.setSelectedQualityIndex(optionId);
        }
    }
}
