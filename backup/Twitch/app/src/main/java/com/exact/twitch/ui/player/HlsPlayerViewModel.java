package com.exact.twitch.ui.player;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.ui.OnQualityChangeListener;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

abstract class HlsPlayerViewModel extends PlayerViewModel implements OnQualityChangeListener {

    HlsPlayerViewModel(Application context, PlayerRepository playerRepository) {
        super(context, playerRepository);
    }

    Callback<ResponseBody> createPlaylistFetchCallback(DataSource.Factory factory) {
        return new Callback<ResponseBody>() {

            private final String TAG = "PlayerRepository";

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Successfully fetched playlist uri");
                //TODO add check if vod is sub only
                HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(factory).createMediaSource(Uri.parse(response.raw().request().url().toString()));
                preparePlayer(hlsMediaSource);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error getting playlist", t);
            }

        };
    }

    @Override
    public void changeQuality(int index) {
        DefaultTrackSelector.ParametersBuilder parametersBuilder = getTrackSelector().buildUponParameters();
        parametersBuilder.setSelectionOverride(VIDEO_RENDERER, getTrackSelector().getCurrentMappedTrackInfo().getTrackGroups(VIDEO_RENDERER), new DefaultTrackSelector.SelectionOverride(0, index));
        getTrackSelector().setParameters(parametersBuilder);
        setPlayerMode(PlayerMode.NORMAL);
    }

    public void enableAutoMode() {
        DefaultTrackSelector.ParametersBuilder parametersBuilder = getTrackSelector().buildUponParameters();
        parametersBuilder.clearSelectionOverrides();
        getTrackSelector().setParameters(parametersBuilder);
        setSelectedQualityIndex(0);
        setPlayerMode(PlayerMode.NORMAL);
    }

    public void setPlayerMode(PlayerMode playerMode) {
        DefaultTrackSelector.ParametersBuilder parametersBuilder = getTrackSelector().buildUponParameters();
        boolean videoDisabled = false; //NORMAL
        boolean audioDisabled = false;
        switch (playerMode) {
            case AUDIO_ONLY:
                videoDisabled = true;
                audioDisabled = false;
                break;
            case DISABLED:
                videoDisabled = audioDisabled = true;
                break;
        }
        parametersBuilder.setRendererDisabled(VIDEO_RENDERER, videoDisabled);
        parametersBuilder.setRendererDisabled(AUDIO_RENDERER, audioDisabled);
        getTrackSelector().setParameters(parametersBuilder);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        HlsManifest hlsManifest = (HlsManifest) manifest;
        if (getQualities().getValue() == null) {
            Pattern pattern = Pattern.compile("NAME=\"(.+)\"");
            HlsMasterPlaylist masterPlaylist = hlsManifest.masterPlaylist;
            List<String> tags = masterPlaylist.tags;
            LinkedHashMap<CharSequence, String> urls = new LinkedHashMap<>(tags.size());
            int trackIndex = 0;
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                Matcher matcher = pattern.matcher(tag);
                if (matcher.find()) {
                    String quality = matcher.group(1);
                    String url = masterPlaylist.variants.get(trackIndex++).url;
                    urls.put(quality.toLowerCase().startsWith("audio") ? "Audio only" : quality, url);
                }
            }
            setUrls(urls);
            LinkedList<CharSequence> list = new LinkedList<>(urls.keySet());
            list.add(list.remove(list.indexOf("Audio only"))); //move audio option to bottom
            getQualities().postValue(list);
        }
    }
}
