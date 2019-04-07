package com.github.exact7.xtra.ui.player

import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.VideoChatMessage
import com.github.exact7.xtra.repository.TwitchService
import com.google.android.exoplayer2.ExoPlayer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.LinkedList
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.math.max

class ChatReplayManager @Inject constructor(
        private val repository: TwitchService,
        private val videoId: String,
        private val startTime: Double,
        private val player: ExoPlayer,
        private val addMessage: (ChatMessage) -> Unit,
        private val clearList: () -> Unit) {

    private var job: Job? = null
    private val timer: Timer
    private var disposable: Disposable? = null
    private var cursor: String? = null
    private val list = LinkedList<VideoChatMessage>()
    private var isLoading = false

    init {
        load(startTime)
        var lastCheckedPosition = 0.0
        timer = fixedRateTimer(period = 1000L, action = {
            val position = currentPosition()
            if (position - lastCheckedPosition !in 0.0..20.0) {
                load(startTime + position)
            }
            lastCheckedPosition = position
        })
    }

    private fun load(offset: Double) {
        if (disposable != null) {
            cancel()
            list.clear()
            clearList.invoke()
        }
        disposable = repository.loadVideoChatLog(videoId, offset)
                .doOnSubscribe { isLoading = true }
                .doOnSuccess { isLoading = false }
                .subscribe({
                    list.addAll(it.messages)
                    cursor = it.next
                    job = GlobalScope.launch {
                        while (true) {
                            val message: VideoChatMessage? = list.poll()
                            if (message != null) {
                                val messageOffset = message.contentOffsetSeconds
                                var position: Double
                                while ((currentPosition() + startTime).also { p -> position = p } < messageOffset) {
                                    delay(max((messageOffset - position) * 1000.0, 0.0).toLong())
                                }
                                if (position - messageOffset < 20.0) {
                                    addMessage.invoke(message)
                                    if (list.size == 15) {
                                        loadNext()
                                    }
                                }
                            } else {
                                if (isLoading) {
                                    delay(1000L)
                                } else if (cursor == null) {
                                    break
                                }
                            }
                        }
                    }
                }, {

                })
    }

    private fun loadNext() {
        cursor?.let { c ->
            disposable = repository.loadVideoChatAfter(videoId, c)
                    .doOnSubscribe { isLoading = true }
                    .doOnSuccess { isLoading = false }
                    .subscribeBy(onSuccess = {
                        list.addAll(it.messages)
                        cursor = it.next
                    })
        }
    }

    fun stop() {
        cancel()
        timer.cancel()
    }

    private fun cancel() {
        disposable?.dispose()
        job?.cancel()
    }

    private fun currentPosition() = runBlocking(Dispatchers.Main) { player.currentPosition / 1000.0 }
}