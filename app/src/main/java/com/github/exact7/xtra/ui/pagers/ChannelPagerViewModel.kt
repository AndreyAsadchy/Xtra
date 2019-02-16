package com.github.exact7.xtra.ui.pagers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.repository.TwitchService
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ChannelPagerViewModel @Inject constructor(
        private val repository: TwitchService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val channelId = MutableLiveData<String>()
    private val _stream = Transformations.switchMap(channelId) {
        repository.loadStream(it, compositeDisposable)
    }
    val stream: LiveData<StreamWrapper>
        get() = _stream

    fun loadStream(channelId: String) {
        if (this.channelId.value != channelId) {
            this.channelId.value = channelId
        }
    }
}
