package com.exact.xtra.ui.player.clip

import android.app.Application
import android.net.Uri
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.OfflineVideo

import com.exact.xtra.model.clip.Clip
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.ui.OnQualityChangeListener
import com.exact.xtra.ui.player.PlayerHelper
import com.exact.xtra.ui.player.PlayerType
import com.exact.xtra.ui.player.PlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.exact.xtra.util.PlayerUtils
import com.exact.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.experimental.launch

import javax.inject.Inject

class ClipPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val dao: VideosDao) : PlayerViewModel(context, PlayerType.VIDEO), OnQualityChangeListener {

    lateinit var clip: Clip
    private val factory: ExtractorMediaSource.Factory = ExtractorMediaSource.Factory(CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory))
    private var playbackProgress: Long = 0
    val helper = PlayerHelper()

    override fun changeQuality(index: Int, tag: String) {
        if (helper.selectedQualityIndex != index) {
            playbackProgress = player.currentPosition
            play(helper.urls[helper.qualities.value!![index]]!!)
        }
    }

    fun play() {
        playerRepository.fetchClipQualities(clip.slug)
                .subscribe({
                    println(it)
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

    private fun play(source: String) {
        play(factory.createMediaSource(Uri.parse(source)))
        player.seekTo(playbackProgress)
    }

    fun download(quality: String) {
        val context = getApplication<Application>()
        val url = helper.urls[quality]!!
        val downloadAction = ProgressiveDownloadAction(Uri.parse(url), false, null, null)
        val uploadDate = TwitchApiHelper.parseIso8601Date(context, clip.createdAt)
        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
        OfflineVideo(url, clip.title, clip.broadcaster.name, clip.game, clip.duration.toLong(), currentDate, uploadDate, clip.thumbnails.medium, clip.broadcaster.logo).let {
            launch { dao::insert }
            PlayerUtils.startDownload(context, downloadAction, it)
        }
    }
}
