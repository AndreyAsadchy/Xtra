package com.exact.twitch.ui.streams;

import androidx.annotation.Nullable;

import com.exact.twitch.model.stream.Stream;
import com.exact.twitch.repository.TwitchService;
import com.exact.twitch.ui.viewmodel.PagedListViewModel;

import javax.inject.Inject;

public class StreamsViewModel extends PagedListViewModel<Stream> {

    @Inject
    StreamsViewModel(TwitchService repository) {
        super(repository);
    }

    public void loadStreams(@Nullable String game, @Nullable String languages, @Nullable String streamType) {
        loadData(repository.loadStreams(game, languages, streamType), false);
    }

    public void loadFollowedStreams(String userToken, @Nullable String streamType) {
        loadData(repository.loadFollowedStreams(userToken, streamType), false);
    }
}
