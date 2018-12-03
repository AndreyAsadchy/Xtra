package com.github.exact7.xtra.ui.videos.followed


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.video.Video
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import com.github.exact7.xtra.ui.videos.BroadcastType
import com.github.exact7.xtra.ui.videos.Sort
import javax.inject.Inject

class FollowedVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Video>() {

    val sortOptions = listOf(R.string.upload_date, R.string.view_count)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        repository.loadFollowedVideos(it.user.token, it.broadcastType, it.language, it.sort, compositeDisposable)
    }
    var selectedIndex = 0
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun setUser(user: User) {
        filter.value = filter.value?.copy(user = user) ?: Filter(user)
    }

    fun sort(sort: Sort, index: Int, text: CharSequence) {
        val filter = filter.value
        if (filter?.sort != sort) {
            this.filter.value = null
            this.filter.value = filter?.copy(sort = sort)
            selectedIndex = index
            _sortText.value = text
        }
    }

    private data class Filter(
            val user: User,
            val broadcastType: BroadcastType = BroadcastType.ALL,
            val sort: Sort = Sort.TIME,
            val language: String? = null)
}
