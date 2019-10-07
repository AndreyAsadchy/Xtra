package com.github.exact7.xtra.ui.player

import com.github.exact7.xtra.model.chat.VideoChatMessage
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.math.max

class ChatReplayManager @Inject constructor(
        private val repository: TwitchService,
        private val videoId: String,
        private val startTime: Double,
        private val currentPosition: () -> Double,
        private val messageListener: OnChatMessageReceivedListener,
        private val clearMessages: () -> Unit) {

    private val timer: Timer
    private var job: Job? = null
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

    fun stop() {
        cancel()
        timer.cancel()
    }

    private fun load(offset: Double) {
        if (disposable != null) {
            cancel()
            list.clear()
            clearMessages()
        }
        disposable = repository.loadVideoChatLog(videoId, offset)
                .doOnSubscribe { isLoading = true }
                .doOnSuccess { isLoading = false }
                .subscribe({
                    it.messages.forEach {
                        println("${it.id} ${it.message}")
                    }
                    list.addAll(it.messages)
                    cursor = it.next
                    job = GlobalScope.launch {
                        while (isActive) {
                            val message: VideoChatMessage? = try {
                                list.poll()
                            } catch (e: NoSuchElementException) { //wtf?
                                null
                            }
                            if (message != null) {
                                val messageOffset = message.contentOffsetSeconds
                                var position: Double
                                while ((currentPosition() + startTime).also { p -> position = p } < messageOffset) {
                                    delay(max((messageOffset - position) * 1000.0, 0.0).toLong())
                                }
                                if (position - messageOffset < 20.0) {
                                    messageListener.onMessage(message)
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

    private fun cancel() {
        disposable?.dispose()
        job?.cancel()
    }
}