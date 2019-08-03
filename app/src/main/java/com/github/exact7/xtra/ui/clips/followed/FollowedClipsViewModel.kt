package com.github.exact7.xtra.ui.clips.followed

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedClipsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Clip>() {

    val sortOptions = listOf(R.string.trending, R.string.view_count)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Clip>> = Transformations.map(filter) {
        repository.loadFollowedClips(it.user.token, it.trending, compositeDisposable)
    }
    var selectedIndex = 1
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun setUser(user: User) {
        if (filter.value?.user != user) {
            filter.value = filter.value?.copy(user = user) ?: Filter(user)
        }
    }

    fun setTrending(trending: Boolean, index: Int, text: CharSequence) {
        filter.value = filter.value?.copy(trending = trending)
        _sortText.value = text
        selectedIndex = index
    }

    private data class Filter(
            val user: User,
            val trending: Boolean = false)
}
