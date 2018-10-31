package com.github.exact7.xtra.ui.player.clip

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.service.ClipDownloadService
import com.github.exact7.xtra.ui.OnQualityChangeListener
import com.github.exact7.xtra.ui.player.PlayerHelper
import com.github.exact7.xtra.ui.player.PlayerViewModel
import com.github.exact7.xtra.util.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository) : PlayerViewModel(context), OnQualityChangeListener {

    private companion object {
        const val TAG = "ClipPlayerViewModel"
    }

    lateinit var clip: Clip
    private val factory: ExtractorMediaSource.Factory = ExtractorMediaSource.Factory(dataSourceFactory)
    private var playbackProgress: Long = 0
    val helper = PlayerHelper()
    private val prefs = context.getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)

    override fun changeQuality(index: Int) {
        playbackProgress = player.currentPosition
        val quality = helper.qualities.value!![index]
        play(helper.urls[quality]!!)
        prefs.edit { putString(TAG, quality.toString()) }
        helper.selectedQualityIndex = index
    }

    override fun play() {
        super.play()
        player.seekTo(playbackProgress)
    }

    fun init() {
        playerRepository.fetchClipQualities(clip.slug)
                .subscribe({
                    val qualities = ArrayList<CharSequence>(it.size)
                    it.forEach { option ->
                        val quality = option.quality + "p"
                        qualities.add(quality)
                        helper.urls[quality] = option.source
                    }
                    play(helper.urls[qualities[0]]!!)
                    helper.qualities.postValue(qualities)
                    helper.selectedQualityIndex = 0
//                    if (clip.vod != null) {
//                        playerRepository.fetchSubscriberBadges(clip.broadcaster.id)
//                                .subscribe({ response ->
//
//                                }, { t ->
//
//                                })
//                                .addTo(compositeDisposable)
//
//                    }
                }, {

                })
                .addTo(compositeDisposable)
    }

//    override fun play() {
//        super.play()
//        player.seekTo(playbackProgress)
//    }

    private fun play(source: String) {
        mediaSource = factory.createMediaSource(Uri.parse(source))
        play()
        player.seekTo(playbackProgress)
    }

    fun download(quality: String) {
        val context = getApplication<Application>()
        Intent(context, ClipDownloadService::class.java).apply {
            putExtra("clip", clip)
            putExtra("quality", quality)
            putExtra("url", helper.urls[quality]!!)
            context.startService(this)
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }
}
