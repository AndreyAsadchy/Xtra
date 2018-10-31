package com.github.exact7.xtra.ui.clips

import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.ui.Initializable
import com.github.exact7.xtra.ui.common.PagedListViewModel
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
