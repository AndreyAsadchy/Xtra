package com.github.exact7.xtra.ui.videos


import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.video.Video
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.Initializable
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class VideosViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Video>(), Initializable {

    val sortText = MutableLiveData<CharSequence>()
    lateinit var sort: Sort
    var period: Period? = null
    var selectedIndex = 0

    fun loadVideos(game: String? = null, broadcastType: BroadcastType = BroadcastType.ALL, language: String? = null, reload: Boolean) {
        loadData(repository.loadVideos(game, period ?: Period.WEEK, broadcastType, language, if (isInitialized()) sort else Sort.VIEWS, compositeDisposable), reload)
    }

    fun loadFollowedVideos(userToken: String, broadcastTypes: BroadcastType = BroadcastType.ALL, language: String? = null, reload: Boolean) {
        loadData(repository.loadFollowedVideos(userToken, broadcastTypes, language, if (isInitialized()) sort else Sort.VIEWS, compositeDisposable), reload)
    }

    fun loadChannelVideos(channelId: Any, broadcastTypes: BroadcastType = BroadcastType.ALL, reload: Boolean) {
        loadData(repository.loadChannelVideos(channelId, broadcastTypes, if (isInitialized()) sort else Sort.TIME, compositeDisposable), reload)
    }

    override fun isInitialized(): Boolean {
        return sortText.value != null && this::sort.isInitialized
    }
}
