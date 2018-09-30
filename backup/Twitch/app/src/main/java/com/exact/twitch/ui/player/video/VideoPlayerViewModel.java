package com.exact.twitch.ui.player.video;

import android.app.Application;
import android.net.Uri;
import android.text.format.DateUtils;

import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.model.video.Video;
import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.repository.TwitchService;
import com.exact.twitch.ui.player.HlsPlayerViewModel;
import com.exact.twitch.util.DownloadUtils;
import com.exact.twitch.util.PlayerUtils;
import com.exact.twitch.util.TwitchApiHelper;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadAction;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class VideoPlayerViewModel extends HlsPlayerViewModel {

    private final TwitchService twitchRepository;
    private Video video;
    private List<HlsMediaPlaylist.Segment> segments;

    @Inject
    public VideoPlayerViewModel(Application context, PlayerRepository playerRepository, TwitchService twitchRepository) {
        super(context, playerRepository);
        this.twitchRepository = twitchRepository;
    }

    public void setVideo(Video video) {
        if (this.video == null) {
            this.video = video;
        }
    }
    CacheDataSourceFactory factory = new CacheDataSourceFactory(DownloadUtils.INSTANCE.getCache(getApplication()), getDataSourceFactory());

    public void play() {
        playerRepository.fetchVideoPlaylist(video.getId(), createPlaylistFetchCallback(factory));
//        initMessages();
    }

    public void download(String quality, List<RenditionKey> keys) {
        String url = getUrls().get(quality);
        HlsDownloadAction hlsDownloadAction = new HlsDownloadAction(Uri.parse(url), false, null, keys);
        String uploadDate = TwitchApiHelper.INSTANCE.parseIso8601Date(getApplication(), video.getCreatedAt());
        String currentDate = DateUtils.formatDateTime(getApplication(), Calendar.getInstance().getTime().getTime(), DateUtils.FORMAT_NO_YEAR);
        OfflineVideo offlineVideo = new OfflineVideo(url, video.getTitle(), video.getChannel().getName(), video.getGame(), video.getLength(), uploadDate, currentDate, video.getPreview(), video.getChannel().getLogo());
//        new Thread(() -> database.videos().insert(new OfflineVideo(url, video.getTitle(), video.getChannel().getName(), video.getGame(), video.getLength(), uploadDate, currentDate, video.getPreview(), video.getChannel().getLogo()))).start(); //TODO maybe change Thread and add custom name for video\
        PlayerUtils.INSTANCE.startDownload(getApplication(), hlsDownloadAction, offlineVideo);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        super.onTimelineChanged(timeline, manifest, reason);
        if (isFirstLaunch()) {
            HlsManifest hlsManifest = (HlsManifest) manifest;
            segments = hlsManifest.mediaPlaylist.segments;
        }
    }

    public List<HlsMediaPlaylist.Segment> getSegments() {
        return segments;
    }
}
