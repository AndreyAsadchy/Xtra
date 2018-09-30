package com.exact.twitch.ui.player;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.exact.twitch.R;
import com.exact.twitch.model.chat.ChatMessage;
import com.exact.twitch.repository.PlayerRepository;
import com.exact.twitch.util.chat.OnChatMessageReceived;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class PlayerViewModel extends AndroidViewModel implements Player.EventListener, OnChatMessageReceived {

    static final int VIDEO_RENDERER = 0;
    static final int AUDIO_RENDERER = 1;
    final PlayerRepository playerRepository;

    private MutableLiveData<LinkedList<CharSequence>> qualities;
    private MutableLiveData<List<ChatMessage>> chatMessages;
    private MutableLiveData<ChatMessage> newMessage;
    private SimpleExoPlayer player;
    private Map<CharSequence, String> urls;
    private Integer selectedQualityIndex;
    private DataSource.Factory dataSourceFactory;
    private DefaultTrackSelector trackSelector;

    public enum PlayerMode {
        NORMAL, AUDIO_ONLY, DISABLED
    }

    public PlayerViewModel(Application context, PlayerRepository playerRepository) {
        super(context);
        this.playerRepository = playerRepository;
        initPlayer();
        qualities = new MutableLiveData<>();
    }

    private void initPlayer() {
        if (player == null) {
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            Application context = getApplication();
            dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)), bandwidthMeter);
            SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(context), trackSelector, new DefaultLoadControl());
            exoPlayer.addListener(this);
            player = exoPlayer;
        }
    }

    void preparePlayer(MediaSource mediaSource) {
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    void initMessages() {
        chatMessages = new MutableLiveData<>();
        newMessage = new MutableLiveData<>();
        chatMessages.postValue(new ArrayList<>());
    }

    public boolean isFirstLaunch() {
        return qualities.getValue() == null;
    }

    public MutableLiveData<LinkedList<CharSequence>> getQualities() {
        return qualities;
    }

    public MutableLiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public MutableLiveData<ChatMessage> getNewMessage() {
        return newMessage;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    Map<CharSequence, String> getUrls() {
        return urls;
    }

    void setUrls(Map<CharSequence, String> urls) {
        this.urls = urls;
    }

    public Integer getSelectedQualityIndex() {
        if (selectedQualityIndex == null) {
            int prefferedQualityIndex = 0;//TODO change to last selected quality
            selectedQualityIndex = prefferedQualityIndex != -1 ? prefferedQualityIndex : 0; //else auto
        }
        return selectedQualityIndex;
    }

    public void setSelectedQualityIndex(Integer selectedQualityIndex) {
        this.selectedQualityIndex = selectedQualityIndex;
    }

    DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    //OnChatMessageReceived

    @Override
    public void onMessage(ChatMessage message) {
        chatMessages.getValue().add(message);
        newMessage.postValue(message);
    }

    //Player.EventListener

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}