package com.exact.xtra.ui.videos


import androidx.lifecycle.MutableLiveData
import com.exact.xtra.model.video.Video
import com.exact.xtra.repository.TwitchService
import com.exact.xtra.ui.Initializable
import com.exact.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class VideosViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Video>(), Initializable {

    val sortText = MutableLiveData<CharSequence>()
    lateinit var sort: Sort

    val periodText by lazy { MutableLiveData<CharSequence>() }
    var period: Period? = null

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
