package com.exact.twitch.ui.videos;


import androidx.lifecycle.MutableLiveData;
import androidx.annotation.Nullable;

import com.exact.twitch.model.video.Video;
import com.exact.twitch.repository.TwitchService;
import com.exact.twitch.ui.viewmodel.PagedListViewModel;

import javax.inject.Inject;

public class VideosViewModel extends PagedListViewModel<Video> {

    private MutableLiveData<CharSequence> currentOrderText = new MutableLiveData<>();
    private Integer selectedOrderItem;
    private String sort;

    private MutableLiveData<CharSequence> currentPeriodText;
    private Integer selectedPeriodItem;
    private String period;

    @Inject
    VideosViewModel(TwitchService repository) {
        super(repository);
    }

    public void loadVideos(@Nullable String game, @Nullable String period, @Nullable String broadcastTypes, @Nullable String language, @Nullable String sort, boolean reload) {
        loadData(repository.loadVideos(game, period, broadcastTypes, language, sort), reload);
    }

    public void loadFollowedVideos(String userToken, @Nullable String broadcastTypes, @Nullable String language, @Nullable String sort, boolean reload) {
        loadData(repository.loadFollowedVideos(userToken, broadcastTypes, language, sort), reload);
    }

    public void loadChannelVideos(Object channelId, @Nullable String broadcastTypes, @Nullable String sort, boolean reload) {
        loadData(repository.loadChannelVideos(channelId, broadcastTypes, sort), reload);
    }

    public MutableLiveData<CharSequence> getCurrentOrderText() {
        return currentOrderText;
    }

    public MutableLiveData<CharSequence> getCurrentPeriodText() {
        if (currentPeriodText == null) {
            currentPeriodText = new MutableLiveData<>();
        }
        return currentPeriodText;
    }

    public Integer getSelectedOrderItem() {
        return selectedOrderItem;
    }

    public void setSelectedOrderItem(Integer selectedOrderItem) {
        this.selectedOrderItem = selectedOrderItem;
    }

    public void setSelectedPeriodItem(Integer selectedPeriodItem) {
        this.selectedPeriodItem = selectedPeriodItem;
    }

    public Integer getSelectedPeriodItem() {
        return selectedPeriodItem;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getPeriod() {
        if (period == null) {
            period = "week";
        }
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
