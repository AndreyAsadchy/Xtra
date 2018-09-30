package com.exact.twitch.ui.player.stream;

import android.app.Application;
import androidx.annotation.Nullable;

import com.exact.twitch.model.channel.Channel;
import com.exact.twitch.model.chat.SubscriberBadgesResponse;
import com.exact.twitch.model.stream.Stream;
import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.ui.player.HlsPlayerViewModel;
import com.exact.twitch.util.TwitchApiHelper;
import com.exact.twitch.util.chat.OnChatConnectedListener;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamPlayerViewModel extends HlsPlayerViewModel {

    private Stream stream;

    @Inject
    public StreamPlayerViewModel(Application context, PlayerRepository playerRepository) {
        super(context, playerRepository);
    }

    @Override
    public boolean isFirstLaunch() {
        return stream == null;
    }

    public void setStream(Stream stream) {
        if (this.stream == null) {
            this.stream = stream;
        }
    }

    public void play(@Nullable String userName, @Nullable String userToken, OnChatConnectedListener callback) {
        Channel channel = stream.getChannel();
        playerRepository.fetchStreamPlaylist(channel.getName(), createPlaylistFetchCallback(getDataSourceFactory()));
        initMessages();
        playerRepository.fetchSubscriberBadges(stream.getChannel().getId(), new Callback<SubscriberBadgesResponse>() {

            @Override
            public void onResponse(Call<SubscriberBadgesResponse> call, Response<SubscriberBadgesResponse> response) {
                if (response.isSuccessful()) {
                    callback.onConnect(TwitchApiHelper.INSTANCE.startChat(stream.getChannel().getName(), userName, userToken, response.body().getBadges(), StreamPlayerViewModel.this));
                }
            }

            @Override
            public void onFailure(Call<SubscriberBadgesResponse> call, Throwable t) {

            }
        });
    }
}
