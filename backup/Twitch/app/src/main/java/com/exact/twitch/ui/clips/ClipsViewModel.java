package com.exact.twitch.ui.clips;


import androidx.lifecycle.MutableLiveData;
import androidx.annotation.Nullable;

import com.exact.twitch.model.clip.Clip;
import com.exact.twitch.repository.TwitchService;
import com.exact.twitch.ui.viewmodel.PagedListViewModel;

import javax.inject.Inject;

public class ClipsViewModel extends PagedListViewModel<Clip> {

    private MutableLiveData<CharSequence> sortText = new MutableLiveData<>();
    private Integer selectedSortItem;
    private String period;
    private boolean trending;

    @Inject
    ClipsViewModel(TwitchService repository) {
        super(repository);
    }

    public void loadClips(@Nullable String channelName, @Nullable String gameName, @Nullable String languages, @Nullable String period, @Nullable Boolean trending, boolean reload) {
        loadData(repository.loadClips(channelName, gameName, languages, period, trending), reload);
    }

    public void loadFollowedClips(String userToken, @Nullable Boolean trending, boolean reload) {
        loadData(repository.loadFollowedClips(userToken, trending), reload);
    }

    public MutableLiveData<CharSequence> getSortText() {
        return sortText;
    }

    public Integer getSelectedSortItem() {
        return selectedSortItem;
    }

    public void setSelectedSortItem(Integer selectedSortItem) {
        this.selectedSortItem = selectedSortItem;
    }

    public boolean getTrending() {
        return trending;
    }

    public void setTrending(boolean trending) {
        this.trending = trending;
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
