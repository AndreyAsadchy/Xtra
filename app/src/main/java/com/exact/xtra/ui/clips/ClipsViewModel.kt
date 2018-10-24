package com.exact.xtra.ui.clips

import androidx.lifecycle.MutableLiveData
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.repository.TwitchService
import com.exact.xtra.ui.Initializable
import com.exact.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Clip>(), Initializable {

    val sortText = MutableLiveData<CharSequence>()
    var period: Period? = null
    var trending: Boolean = false
    var selectedIndex = 0

    fun loadClips(channelName: String? = null, gameName: String? = null, languages: String? = null, period: Period? = this.period, trending: Boolean = this.trending, reload: Boolean) {
        loadData(repository.loadClips(channelName, gameName, languages, period ?: Period.WEEK, trending, compositeDisposable), reload)
    }

    fun loadFollowedClips(userToken: String, trending: Boolean = this.trending, reload: Boolean) {
        loadData(repository.loadFollowedClips(userToken, trending, compositeDisposable), reload)
    }

    override fun isInitialized(): Boolean {
        return sortText.value != null
    }
}
