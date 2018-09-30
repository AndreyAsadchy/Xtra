package com.exact.twitch.ui.player.clip;

import android.app.Application;
import android.net.Uri;
import android.text.format.DateUtils;

import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.model.clip.Clip;
import com.exact.twitch.model.clip.ClipStatusResponse;
import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.ui.OnQualityChangeListener;
import com.exact.twitch.ui.player.PlayerViewModel;
import com.exact.twitch.util.DownloadUtils;
import com.exact.twitch.util.PlayerUtils;
import com.exact.twitch.util.TwitchApiHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClipPlayerViewModel extends PlayerViewModel implements OnQualityChangeListener {

    private Clip clip;
    private ExtractorMediaSource.Factory factory;
    private long playbackProgress;

    @Inject
    public ClipPlayerViewModel(Application context, PlayerRepository playerRepository) {
        super(context, playerRepository);
    }

    @Override
    public void changeQuality(int index) {
        playbackProgress = getPlayer().getCurrentPosition();
        play(getUrls().get(getQualities().getValue().get(index)));
    }

    @Override
    public boolean isFirstLaunch() {
        return clip == null;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public void play() {
        getQualities().setValue(new LinkedList<>());
        playerRepository.fetchClipQualities(clip.getSlug(), new Callback<ClipStatusResponse>() {
            @Override
            public void onResponse(Call<ClipStatusResponse> call, Response<ClipStatusResponse> response) {
                if (response.isSuccessful()) {
                    Map<CharSequence, String> urls = new HashMap<>();
                    factory = new ExtractorMediaSource.Factory(new CacheDataSourceFactory(DownloadUtils.INSTANCE.getCache(getApplication()), getDataSourceFactory()));
                    for (ClipStatusResponse.QualityOption qualityOption : response.body().getQualityOptions()) {
                        urls.put(qualityOption.getQuality(), qualityOption.getSource());
                        getQualities().getValue().add(qualityOption.getQuality());
                    }
                    setUrls(urls);
                    play(response.body().getQualityOptions().get(0).getSource());

                }
            }

            @Override
            public void onFailure(Call<ClipStatusResponse> call, Throwable t) {

            }
        });
//            if (clip.getVod() != null) {
//                initMessages();
//                fetchSubscriberBadges(Integer.parseInt(clip.getBroadcaster().getId()));
//            }
    }

    private void play(String source) {
        MediaSource mediaSource = factory.createMediaSource(Uri.parse(source));
        getPlayer().prepare(mediaSource);
        getPlayer().seekTo(playbackProgress);
        getPlayer().setPlayWhenReady(true);
    }

    public void download(String quality) {
        String url = getUrls().get(quality);
        ProgressiveDownloadAction progressiveDownloadAction = new ProgressiveDownloadAction(Uri.parse(url), false, null, null);
        OfflineVideo video = new OfflineVideo(url, clip.getTitle(), clip.getBroadcaster().getName(), clip.getGame(), clip.getDuration().longValue(), TwitchApiHelper.INSTANCE.parseIso8601Date(getApplication(), clip.getCreatedAt()), DateUtils.formatDateTime(getApplication(), Calendar.getInstance().getTime().getTime(), DateUtils.FORMAT_NO_YEAR), clip.getThumbnails().getMedium(), clip.getBroadcaster().getLogo());
        PlayerUtils.INSTANCE.startDownload(getApplication(), progressiveDownloadAction, video);
    }
}
