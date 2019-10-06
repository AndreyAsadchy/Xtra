package com.github.exact7.xtra.ui.videos.channel


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.videos.BaseVideosViewModel
import javax.inject.Inject

class ChannelVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository) : BaseVideosViewModel(playerRepository) {

    val sortOptions = listOf(R.string.upload_date, R.string.view_count)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        repository.loadChannelVideos(it.channelId, BroadcastType.ALL, it.sort, compositeDisposable)
    }
    var selectedIndex = 0
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun setChannelId(channelId: String) {
        if (filter.value?.channelId != channelId) {
            filter.value = Filter(channelId)
        }
    }

    fun setSort(sort: Sort, index: Int, text: CharSequence) {
        filter.value = filter.value?.copy(sort = sort)
        selectedIndex = index
        _sortText.value = text
    }

    private data class Filter(
            val channelId: String,
            val sort: Sort = Sort.TIME)
}
