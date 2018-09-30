package com.exact.twitch.ui.player.offline;

import android.app.Application;
import android.net.Uri;

import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.ui.player.PlayerViewModel;
import com.exact.twitch.util.DownloadUtils;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;

import javax.inject.Inject;

public class OfflinePlayerViewModel extends PlayerViewModel {

    private OfflineVideo video;

    @Inject
    public OfflinePlayerViewModel(Application context, PlayerRepository playerRepository) {
        super(context, playerRepository);
    }

    public void play() {
        MediaSource mediaSource;
        Uri uri = Uri.parse(video.getUrl());
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(DownloadUtils.INSTANCE.getCache(getApplication()), getDataSourceFactory());
        if (video.getUrl().endsWith("mp4")) {
            mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri);
        } else {
            mediaSource = new HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri);
        }
        preparePlayer(mediaSource);
    }

    public void setVideo(OfflineVideo video) {
        this.video = video;
    }
}
