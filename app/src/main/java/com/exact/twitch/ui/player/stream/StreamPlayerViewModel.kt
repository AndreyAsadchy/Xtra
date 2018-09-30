package com.exact.twitch.ui.player.stream

import android.app.Application
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.repository.PlayerRepository
import com.exact.twitch.tasks.LiveChatTask
import com.exact.twitch.ui.player.HlsPlayerViewModel
import com.exact.twitch.util.TwitchApiHelper
import com.exact.twitch.util.chat.OnChatConnectedListener
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StreamPlayerViewModel @Inject constructor(
        context: Application,
        private val repository: PlayerRepository) : HlsPlayerViewModel(context) {

    lateinit var chatThread: LiveChatTask
    var userToken: String? = null

    fun play(stream: Stream, userName: String?, userToken: String?, callback: OnChatConnectedListener) {
        val channel = stream.channel
        repository.fetchStreamPlaylist(channel.name)
                .subscribe({
                    play(HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it))
                }, {

                })
                .addTo(compositeDisposable)
        repository.fetchSubscriberBadges(channel.id.toInt())
                .subscribe({
                    callback.onConnect(TwitchApiHelper.startChat(channel.name, userName, userToken, it, helper).also { chat ->
//                        chatThread = chat
                    })
                }, {

                })
                .addTo(compositeDisposable)
    }

    fun isUserAuthorized() = userToken != null

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
//        chatThread.shutdown()
    }
}
