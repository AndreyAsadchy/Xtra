package com.exact.twitch.ui.downloads;

import androidx.paging.PagedList;

import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.repository.OfflineRepository;

import javax.inject.Inject;

public class DownloadsViewModel extends PagedListViewModel<OfflineVideo> {

    @Inject
    DownloadsViewModel(OfflineRepository repository) {
        super(null);
        System.out.println("cal");
        loadData(repository.loadDownloadedVideos(), false);
    }
}
