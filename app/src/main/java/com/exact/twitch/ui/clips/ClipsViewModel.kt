package com.exact.twitch.ui.clips

import androidx.lifecycle.MutableLiveData
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.repository.TwitchService
import com.exact.twitch.ui.Initializable
import com.exact.twitch.ui.common.PagedListViewModel
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Clip>(), Initializable {

    val sortText = MutableLiveData<CharSequence>()
    var period: Period? = null
    var trending: Boolean = false

    fun loadClips(channelName: String? = null, gameName: String? = null, languages: String? = null, reload: Boolean) {
        loadData(repository.loadClips(channelName, gameName, languages, period ?: Period.WEEK, trending, compositeDisposable), reload)
    }

    fun loadFollowedClips(userToken: String, reload: Boolean) {
        loadData(repository.loadFollowedClips(userToken, trending, compositeDisposable), reload)
    }

    override fun isInitialized(): Boolean {
        return sortText.value == null
    }
}
