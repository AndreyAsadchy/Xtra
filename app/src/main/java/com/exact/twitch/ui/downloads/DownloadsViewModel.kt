package com.exact.twitch.ui.downloads


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.repository.OfflineRepository
import javax.inject.Inject

class DownloadsViewModel @Inject internal constructor(
        private val repository: OfflineRepository) : ViewModel() {

    val loaded = MutableLiveData<Boolean>()

    fun load() = repository.loadAll().also { loaded.postValue(true) }

    fun delete(video: OfflineVideo) = repository.delete(video)
}
