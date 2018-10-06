package com.exact.xtra.ui.player.video

import android.app.Application
import android.net.Uri
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.model.video.Video

import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.repository.TwitchService
import com.exact.xtra.ui.player.HlsPlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.exact.xtra.util.PlayerUtils
import com.exact.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadAction
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.experimental.launch

import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val twitchRepository: TwitchService,
        private val dao: VideosDao) : HlsPlayerViewModel(context) {

    lateinit var video: Video
    lateinit var segments: List<HlsMediaPlaylist.Segment>
    private val cacheDataSourceFactory = CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory)

    fun play() {
        playerRepository.fetchVideoPlaylist(video.id)
                .subscribe({
                    play(HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(it))
                }, {

                })
                .addTo(compositeDisposable)
    }

    fun download(quality: String, keys: List<RenditionKey>) {
        val context = getApplication<Application>()
        val url = helper.urls[quality] as String
        val hlsDownloadAction = HlsDownloadAction(Uri.parse(url), false, null, keys)
        val uploadDate = TwitchApiHelper.parseIso8601Date(context, video.createdAt)
        val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
        //TODO maybe add custom name for video
        OfflineVideo(url, video.title, video.channel.name, video.game, video.length.toLong(), currentDate, uploadDate, video.preview.medium, video.channel.logo).let {
            launch { dao::insert }
            PlayerUtils.startDownload(context, hlsDownloadAction, it)
        }
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        super.onTimelineChanged(timeline, manifest, reason)
        if (!this::segments.isInitialized) {
            segments = (manifest as HlsManifest).mediaPlaylist.segments
        }
    }
}
