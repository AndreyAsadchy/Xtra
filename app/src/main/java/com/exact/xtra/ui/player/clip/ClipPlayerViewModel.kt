package com.exact.xtra.ui.player.clip

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.service.ClipDownloadService
import com.exact.xtra.ui.OnQualityChangeListener
import com.exact.xtra.ui.player.PlayerHelper
import com.exact.xtra.ui.player.PlayerType
import com.exact.xtra.ui.player.PlayerViewModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository) : PlayerViewModel(context, PlayerType.VIDEO), OnQualityChangeListener {

    lateinit var clip: Clip
    private val factory: ExtractorMediaSource.Factory = ExtractorMediaSource.Factory(dataSourceFactory)
    private var playbackProgress: Long = 0
    val helper = PlayerHelper(0)

    override fun changeQuality(index: Int) {
        if (helper.selectedQualityIndex != index) {
            playbackProgress = player.currentPosition
            play(helper.urls[helper.qualities.value!![index]]!!)
        }
    }

    fun init() {
        playerRepository.fetchClipQualities(clip.slug)
                .subscribe({
                    val qualities = ArrayList<CharSequence>(it.size)
                    it.forEach { option ->
                        qualities.add(option.quality)
                        helper.urls[option.quality] = option.source
                    }
                    play(helper.urls[qualities[helper.selectedQualityIndex]]!!)
                    helper.qualities.postValue(qualities)
                    if (clip.vod != null) {
                        playerRepository.fetchSubscriberBadges(clip.broadcaster.id.toInt())
                                .subscribe({ response ->

                                }, { t ->

                                })
                                .addTo(compositeDisposable)

                    }
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
