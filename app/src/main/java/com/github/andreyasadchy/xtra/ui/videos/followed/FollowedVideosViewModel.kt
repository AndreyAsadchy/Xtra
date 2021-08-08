package com.github.andreyasadchy.xtra.ui.videos.followed


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.kraken.video.BroadcastType
import com.github.andreyasadchy.xtra.model.kraken.video.Sort
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosViewModel
import javax.inject.Inject

class FollowedVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository) : BaseVideosViewModel(playerRepository) {

    val sortOptions = listOf(R.string.upload_date, R.string.view_count)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        repository.loadFollowedVideos(it.user.token, it.broadcastType, it.language, it.sort, viewModelScope)
    }
    var selectedIndex = 0
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun setUser(user: User) {
        if (filter.value == null) {
            filter.value = Filter(user)
        }
    }

    fun sort(sort: Sort, index: Int, text: CharSequence) {
        filter.value = filter.value?.copy(sort = sort)
        selectedIndex = index
        _sortText.value = text
    }

    private data class Filter(
            val user: User,
            val broadcastType: BroadcastType = BroadcastType.ALL,
            val sort: Sort = Sort.TIME,
            val language: String? = null)
}
