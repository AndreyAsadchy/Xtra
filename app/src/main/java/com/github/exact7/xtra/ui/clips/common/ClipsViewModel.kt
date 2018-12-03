package com.github.exact7.xtra.ui.clips.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.repository.Listing
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.clips.Period
import com.github.exact7.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Clip>() {

    val sortOptions = listOf(R.string.trending, R.string.today, R.string.this_week, R.string.this_month, R.string.all_time)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Clip>> = Transformations.map(filter) {
        repository.loadClips(it.channelName, it.game?.info?.name, it.languages, it.period, it.trending, compositeDisposable)
    }
    var selectedIndex = 2
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun loadClips(channelName: String? = null, game: Game? = null, languages: String? = null) {
        filter.value = Filter(channelName, game, languages)
    }

    fun filter(period: Period?, trending: Boolean, index: Int, text: CharSequence) {
        val newFilter = filter.value?.copy(period = period, trending = trending)
        if (filter.value != newFilter) {
            filter.value = null
            filter.value = newFilter
            _sortText.value = text
            _loadedInitial.value = null
            selectedIndex = index
        }
    }

    private data class Filter(
            val channelName: String?,
            val game: Game?,
            val languages: String?,
            val period: Period? = Period.WEEK,
            val trending: Boolean = false)
}
